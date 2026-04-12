package com.aiagent.service;

import com.aiagent.model.AgentTrace;
import com.aiagent.model.ChatMessage;
import com.aiagent.model.Conversation;
import com.aiagent.model.dto.AgentTraceResponse;
import com.aiagent.model.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core agent service implementing the ReAct (Reasoning + Acting) loop.
 * This is the Java equivalent of LangGraph's create_react_agent.
 *
 * The loop works as follows:
 * 1. THINK: Send user message + conversation history to the LLM with tool definitions
 * 2. ACT: If the LLM responds with tool_calls, execute each tool
 * 3. OBSERVE: Feed tool results back to the LLM
 * 4. REPEAT: Until the LLM produces a final text response (no more tool_calls)
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);
    private static final int MAX_ITERATIONS = 10;

    private static final String SYSTEM_PROMPT =
            "You are a professional multi-tasking agent. " +
            "Your goal is to solve the user's request using the tools provided. " +
            "Logic Rules:\n" +
            "1. Analyze the user query to see which tools are needed.\n" +
            "2. If a task has multiple steps, perform them one by one.\n" +
            "3. Use the output of one tool to inform the next tool call.\n" +
            "4. If you cannot find information, say so clearly.\n" +
            "5. Always be concise and factual.";

    private final OllamaService ollamaService;
    private final ToolExecutorService toolExecutorService;
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public AgentService(OllamaService ollamaService,
                        ToolExecutorService toolExecutorService,
                        ConversationService conversationService,
                        SimpMessagingTemplate messagingTemplate) {
        this.ollamaService = ollamaService;
        this.toolExecutorService = toolExecutorService;
        this.conversationService = conversationService;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Process a user message through the ReAct agent loop.
     */
    public ChatResponse processMessage(String userMessage, String conversationId) {
        long overallStart = System.currentTimeMillis();
        List<AgentTrace> traces = new ArrayList<>();

        try {
            // Get or create conversation
            Conversation conversation;
            if (conversationId != null && !conversationId.isBlank()) {
                conversation = conversationService.getConversation(conversationId);
            } else {
                // Auto-generate title from the first few words
                String title = userMessage.length() > 50
                        ? userMessage.substring(0, 50) + "..."
                        : userMessage;
                conversation = conversationService.createConversation(title);
            }

            // Save user message
            conversationService.addMessage(conversation.getId(), userMessage, ChatMessage.MessageRole.USER);

            // Build message history for context
            List<Map<String, Object>> messages = buildMessageHistory(conversation, userMessage);

            // Get tool definitions
            List<Map<String, Object>> toolDefs = toolExecutorService.getToolDefinitions();

            // ReAct loop
            String finalAnswer = executeReActLoop(messages, toolDefs, conversation.getId(), traces);

            // Save assistant response
            long totalTime = System.currentTimeMillis() - overallStart;
            ChatMessage assistantMsg = conversationService.addMessage(
                    conversation.getId(), finalAnswer, ChatMessage.MessageRole.ASSISTANT);
            assistantMsg.setProcessingTimeMs(totalTime);

            // Broadcast completion via WebSocket
            broadcastTrace(conversation.getId(),
                    new AgentTrace(AgentTrace.TraceType.FINAL_ANSWER, finalAnswer), true);

            return ChatResponse.success(finalAnswer, conversation.getId(), traces, totalTime);

        } catch (Exception e) {
            log.error("Agent processing failed", e);
            return ChatResponse.error("Agent error: " + e.getMessage());
        }
    }

    /**
     * The core ReAct loop — iterates until the LLM produces a final answer.
     */
    private String executeReActLoop(List<Map<String, Object>> messages,
                                     List<Map<String, Object>> toolDefs,
                                     String conversationId,
                                     List<AgentTrace> traces) throws Exception {
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            log.info("ReAct iteration {}", iteration + 1);

            // THINK: Ask the LLM what to do next
            AgentTrace thinkTrace = new AgentTrace(AgentTrace.TraceType.THINKING,
                    "Analyzing and deciding next step...");
            thinkTrace.setStatus(AgentTrace.TraceStatus.RUNNING);
            broadcastTrace(conversationId, thinkTrace, false);

            long llmStart = System.currentTimeMillis();
            JsonNode response = ollamaService.chatWithTools(messages, toolDefs);
            long llmDuration = System.currentTimeMillis() - llmStart;

            JsonNode messageNode = response.path("message");
            String content = messageNode.path("content").asText("");
            JsonNode toolCalls = messageNode.path("tool_calls");

            // If no tool calls, the LLM has reached a final answer
            if (toolCalls.isMissingNode() || !toolCalls.isArray() || toolCalls.isEmpty()) {
                thinkTrace.setContent("Reached final answer");
                thinkTrace.setDurationMs(llmDuration);
                thinkTrace.setStatus(AgentTrace.TraceStatus.COMPLETED);
                traces.add(thinkTrace);

                if (content == null || content.isBlank()) {
                    return "The agent finished processing but returned an empty response.";
                }
                return content;
            }

            thinkTrace.setContent("Decided to call " + toolCalls.size() + " tool(s)");
            thinkTrace.setDurationMs(llmDuration);
            thinkTrace.setStatus(AgentTrace.TraceStatus.COMPLETED);
            traces.add(thinkTrace);

            // Add assistant message with tool calls to history
            Map<String, Object> assistantMessage = new LinkedHashMap<>();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("content", content != null ? content : "");
            assistantMessage.put("tool_calls", objectMapper.treeToValue(toolCalls, List.class));
            messages.add(assistantMessage);

            // ACT: Execute each tool call
            for (JsonNode toolCall : toolCalls) {
                JsonNode function = toolCall.path("function");
                String toolName = function.path("name").asText();
                JsonNode argsNode = function.path("arguments");

                // Parse arguments
                Map<String, String> args = new LinkedHashMap<>();
                if (argsNode.isObject()) {
                    argsNode.fields().forEachRemaining(entry ->
                            args.put(entry.getKey(), entry.getValue().asText()));
                }

                // Broadcast tool call event
                AgentTrace toolTrace = new AgentTrace(AgentTrace.TraceType.TOOL_CALL, "");
                toolTrace.setToolName(toolName);
                toolTrace.setToolInput(args.toString());
                toolTrace.setStatus(AgentTrace.TraceStatus.RUNNING);
                broadcastTrace(conversationId, toolTrace, false);

                // Execute the tool
                long toolStart = System.currentTimeMillis();
                String toolResult = toolExecutorService.executeTool(toolName, args);
                long toolDuration = System.currentTimeMillis() - toolStart;

                // Update trace
                toolTrace.setToolOutput(toolResult.length() > 500
                        ? toolResult.substring(0, 500) + "..." : toolResult);
                toolTrace.setDurationMs(toolDuration);
                toolTrace.setStatus(AgentTrace.TraceStatus.COMPLETED);
                toolTrace.setContent("Tool '" + toolName + "' completed in " + toolDuration + "ms");
                traces.add(toolTrace);

                // Broadcast result
                AgentTrace resultTrace = new AgentTrace(AgentTrace.TraceType.TOOL_RESULT,
                        "Tool '" + toolName + "' finished.");
                resultTrace.setToolName(toolName);
                resultTrace.setToolOutput(toolTrace.getToolOutput());
                resultTrace.setDurationMs(toolDuration);
                resultTrace.setStatus(AgentTrace.TraceStatus.COMPLETED);
                broadcastTrace(conversationId, resultTrace, false);

                // OBSERVE: Add tool result back to messages
                Map<String, Object> toolMessage = new LinkedHashMap<>();
                toolMessage.put("role", "tool");
                toolMessage.put("content", toolResult);
                messages.add(toolMessage);
            }
        }

        return "Agent reached maximum iterations (" + MAX_ITERATIONS + ") without a final answer.";
    }

    /**
     * Build the message history from the conversation for LLM context.
     */
    private List<Map<String, Object>> buildMessageHistory(Conversation conversation, String currentMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // System prompt
        Map<String, Object> systemMsg = new LinkedHashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        // Previous messages (limit to last 20 for context window management)
        List<ChatMessage> history = conversationService.getMessages(conversation.getId());
        int startIdx = Math.max(0, history.size() - 20);
        for (int i = startIdx; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            // Skip the current user message as we'll add it separately
            if (i == history.size() - 1 && msg.getRole() == ChatMessage.MessageRole.USER) {
                continue;
            }
            Map<String, Object> histMsg = new LinkedHashMap<>();
            histMsg.put("role", msg.getRole().name().toLowerCase());
            histMsg.put("content", msg.getContent());
            messages.add(histMsg);
        }

        // Current user message
        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", currentMessage);
        messages.add(userMsg);

        return messages;
    }

    /**
     * Broadcast a trace event to all WebSocket subscribers.
     */
    private void broadcastTrace(String conversationId, AgentTrace trace, boolean isComplete) {
        try {
            trace.setId(UUID.randomUUID().toString());
            AgentTraceResponse event = new AgentTraceResponse(conversationId, trace, isComplete);
            messagingTemplate.convertAndSend("/topic/trace/" + conversationId, event);
        } catch (Exception e) {
            log.warn("Failed to broadcast trace event", e);
        }
    }
}

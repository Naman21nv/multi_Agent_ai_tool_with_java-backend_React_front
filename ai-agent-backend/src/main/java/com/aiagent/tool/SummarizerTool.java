package com.aiagent.tool;

import com.aiagent.service.OllamaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Summarization tool — delegates to Ollama with a lower temperature for factual, concise output.
 * Equivalent to the Python `summarise` tool.
 * Uses a separate LLM invocation to offload summarization from the main agent context.
 */
@Component
public class SummarizerTool implements AgentTool {

    private static final Logger log = LoggerFactory.getLogger(SummarizerTool.class);
    private final OllamaService ollamaService;

    public SummarizerTool(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @Override
    public String getName() {
        return "summarise";
    }

    @Override
    public String getDescription() {
        return "Summarize long text, such as data from a web search, into a concise summary.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> textProp = new HashMap<>();
        textProp.put("type", "string");
        textProp.put("description", "The text to summarize");
        properties.put("text", textProp);

        schema.put("properties", properties);
        schema.put("required", new String[]{"text"});
        return schema;
    }

    @Override
    public String execute(Map<String, String> arguments) throws Exception {
        String text = arguments.get("text");
        if (text == null || text.isBlank()) {
            return "Error: No text provided for summarization.";
        }

        log.info("Summarizing text ({} chars)", text.length());

        String prompt = "Please provide a concise summary of the following text:\n\n" + text;
        return ollamaService.generateSimple(prompt, 0.3);
    }
}

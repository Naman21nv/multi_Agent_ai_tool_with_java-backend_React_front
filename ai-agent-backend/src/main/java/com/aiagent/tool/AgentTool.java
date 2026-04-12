package com.aiagent.tool;

import java.util.Map;

/**
 * Interface that all agent tools must implement.
 * This mirrors the @tool decorator pattern from Python LangChain.
 * Each tool has a name, description (for LLM function calling), and an execute method.
 */
public interface AgentTool {

    /** Unique tool name used in function calling */
    String getName();

    /** Description the LLM reads to decide when to use this tool */
    String getDescription();

    /** JSON schema of the tool's parameters for function calling */
    Map<String, Object> getParameterSchema();

    /** Execute the tool with the given arguments */
    String execute(Map<String, String> arguments) throws Exception;
}

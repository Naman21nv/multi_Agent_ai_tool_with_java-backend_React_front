package com.aiagent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * File writer tool — allows the agent to persist content to the filesystem.
 * Equivalent to the Python `write_to_file` tool.
 * Uses append mode to prevent overwriting previous writes.
 */
@Component
public class FileWriterTool implements AgentTool {

    private static final Logger log = LoggerFactory.getLogger(FileWriterTool.class);
    private static final String OUTPUT_DIR = "agent-output";

    @Override
    public String getName() {
        return "write_to_file";
    }

    @Override
    public String getDescription() {
        return "Write content to a file. The file will be saved in the agent-output directory.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> contentProp = new HashMap<>();
        contentProp.put("type", "string");
        contentProp.put("description", "The content to write to the file");
        properties.put("content", contentProp);

        Map<String, Object> filenameProp = new HashMap<>();
        filenameProp.put("type", "string");
        filenameProp.put("description", "The filename to write to");
        properties.put("filename", filenameProp);

        schema.put("properties", properties);
        schema.put("required", new String[]{"content", "filename"});
        return schema;
    }

    @Override
    public String execute(Map<String, String> arguments) throws Exception {
        String content = arguments.get("content");
        String filename = arguments.get("filename");

        if (content == null || filename == null) {
            return "Error: Both 'content' and 'filename' are required.";
        }

        // Sanitize filename to prevent path traversal
        String sanitized = Paths.get(filename).getFileName().toString();

        try {
            Path outputDir = Paths.get(OUTPUT_DIR);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            Path filePath = outputDir.resolve(sanitized);
            Files.writeString(filePath, content + "\n",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            log.info("Wrote content to file: {}", filePath.toAbsolutePath());
            return "Successfully saved to " + sanitized;

        } catch (IOException e) {
            log.error("Failed to write file: {}", sanitized, e);
            return "Error writing to file: " + e.getMessage();
        }
    }
}

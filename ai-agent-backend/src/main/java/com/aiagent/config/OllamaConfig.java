package com.aiagent.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for the Ollama LLM client.
 * Provides a shared OkHttpClient with appropriate timeouts for LLM inference.
 */
@Configuration
public class OllamaConfig {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    @Value("${ollama.temperature}")
    private double temperature;

    @Bean
    public OkHttpClient ollamaHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)    // LLM inference can be slow
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public String ollamaBaseUrl() {
        return baseUrl;
    }

    @Bean
    public String ollamaModel() {
        return model;
    }

    @Bean
    public double ollamaTemperature() {
        return temperature;
    }
}

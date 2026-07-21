package com.shieldai.scan.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * WebClient-based HTTP client for calling the Python AI Engine (FastAPI).
 * Handles timeouts, error fallback, and response mapping.
 */
@Component
@Slf4j
public class AiEngineClient {

    private final WebClient webClient;
    private final int timeoutMs;

    public AiEngineClient(
            @Value("${app.ai-engine.base-url}") String baseUrl,
            @Value("${app.ai-engine.timeout-ms}") int timeoutMs) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.timeoutMs = timeoutMs;
    }

    /**
     * Send content to the AI engine for risk scoring.
     * Returns the AI response or a fallback response on failure.
     */
    public AiScanResult scanContent(String content, String channel) {
        try {
            Map<String, String> requestBody = Map.of(
                    "content", content,
                    "channel", channel.toLowerCase()
            );

            return webClient.post()
                    .uri("/api/v1/scan")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AiScanResult.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .onErrorResume(ex -> {
                        log.error("AI Engine call failed: {}", ex.getMessage());
                        return Mono.just(AiScanResult.fallback());
                    })
                    .block();

        } catch (Exception ex) {
            log.error("AI Engine unreachable: {}", ex.getMessage());
            return AiScanResult.fallback();
        }
    }

    /**
     * Response model from the Python AI Engine.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AiScanResult {
        private int risk_score;
        private String risk_label;
        private String explanation;

        /**
         * Fallback when AI engine is unavailable — returns SAFE with a note.
         */
        public static AiScanResult fallback() {
            return AiScanResult.builder()
                    .risk_score(0)
                    .risk_label("Safe")
                    .explanation("AI engine unavailable — content not scored. Please retry.")
                    .build();
        }
    }
}

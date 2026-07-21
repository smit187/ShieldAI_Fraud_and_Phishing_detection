package com.shieldai.scan.kafka;

import com.shieldai.scan.event.ScanCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer — publishes scan completion events for downstream processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScanEventProducer {

    private static final String TOPIC = "scan-events";
    private final KafkaTemplate<String, ScanCompletedEvent> kafkaTemplate;

    public void publishScanCompleted(ScanCompletedEvent event) {
        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish scan event for scan {}: {}",
                                event.getScanId(), ex.getMessage());
                    } else {
                        log.debug("Scan event published for scan {} to partition {}",
                                event.getScanId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}

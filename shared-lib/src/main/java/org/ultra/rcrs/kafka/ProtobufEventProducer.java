package org.ultra.rcrs.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.ultra.rcrs.events.common.DomainEventOuterClass;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ProtobufEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public ProtobufEventProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    protected void sendEvent(DomainEventOuterClass.DomainEvent event, String topic) {
        Objects.requireNonNull(event);
        Objects.requireNonNull(topic);

        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(topic, event.toByteArray());
        future.thenAcceptAsync(result ->
                        log.info("Sent message to topic=[{}] with offset=[{}]",
                                result.getProducerRecord().topic(), result.getRecordMetadata().offset()))
                .exceptionallyAsync(err -> {
                    log.error("Unable to send message due to: {}", err.getMessage());
                    return null;
                });
    }
}

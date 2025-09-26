package com.back.domain.party.paryChat.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.dlq.topic:chat-messages-dlq}")
    private String dlqTopic;

    public KafkaConfig(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final long INTERVAL_MS = 1000L;
    private static final long MAX_ATTEMPTS = 3L;

    @Bean
    @SuppressWarnings("unchecked")
    public DefaultErrorHandler errorHandler() {
        FixedBackOff fixedBackOff = new FixedBackOff(INTERVAL_MS, MAX_ATTEMPTS - 1);

        ConsumerRecordRecoverer recoverer = (record, exception) -> {
            ConsumerRecord<String, String> consumerRecord = (ConsumerRecord<String, String>) record;

            log.error(
                    "--- FINAL FAILURE --- Message moved to DLQ. Topic: {}, Partition: {}, Offset: {}, Key: {}, Exception: {}",
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    consumerRecord.key(),
                    exception.getMessage(),
                    exception // 예외 객체를 마지막 인자로 전달하여 스택 트레이스를 로그에 포함시킵니다.
            );

            try {
                // DLQ로 메시지 발행
                kafkaTemplate.send(dlqTopic, consumerRecord.key(), consumerRecord.value()).get();
            } catch (Exception e) {
                // DLQ 전송 실패 시, 심각한 오류 로그 기록
                log.error("CRITICAL FAILURE: Failed to send message to DLQ! Data loss imminent. Original message: {}", consumerRecord.value(), e);
            }
        };

        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }
}
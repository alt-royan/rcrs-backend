package org.ultra.rcrs.workflowservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.ultra.rcrs.kafka.config.KafkaBaseConfig;

@Configuration
@EnableKafka
@Import(KafkaBaseConfig.class)
public class KafkaConfig {
}

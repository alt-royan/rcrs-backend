package org.ultra.rcrs.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.ultra.rcrs.kafka.config.KafkaBaseConfig;

@Configuration
@EnableKafka
@org.springframework.context.annotation.Import(KafkaBaseConfig.class)
public class KafkaConfig {
}

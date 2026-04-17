package org.ultra.rcrs.searchservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.ultra.rcrs.kafka.Topics;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    private Map<String, Object> consumerProps(KafkaProperties kafkaProperties) {
        var consumerProps = kafkaProperties.getConsumer();

        consumerProps.setKeyDeserializer(StringDeserializer.class);
        consumerProps.setValueDeserializer(JacksonJsonDeserializer.class);

        return consumerProps.buildProperties();
    }


    private Map<String, Object> producerProps(KafkaProperties kafkaProperties) {
        var producerProps = kafkaProperties.getProducer();

        producerProps.setKeySerializer(StringSerializer.class);
        producerProps.setValueSerializer(StringSerializer.class);

        return producerProps.buildProperties();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(producerProps(kafkaProperties));
    }

    @Bean
    public KafkaTemplate<String, String> stringTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(consumerProps(kafkaProperties));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public NewTopic indexTopic() {
        return TopicBuilder.name(Topics.SEARCH_INDEX_TOPIC).build();
    }
}
package com.example.all_in_one.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

@Configuration
public class KafkaPubSubConfiguration<K,V> {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;
    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;


    @Value("${spring.kafka.consumer.topic}")
    private String receiverTopic;
    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeSerializer;
    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeSerializer;


    @Bean
    KafkaSender<K,V> kafkaSender() {
        var senderProperties = Map.<String, Object>of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer
                //ProducerConfig.CLIENT_ID_CONFIG, "sample-producer",
                //ProducerConfig.ACKS_CONFIG, "all"
        );
        SenderOptions<K,V> senderOptions =
                SenderOptions.<K,V>create(senderProperties)
                        .maxInFlight(1024);
        return KafkaSender.create(senderOptions);
    }

    @Bean
    KafkaReceiver<K,V> kafkaReceiver() {
        var receiverProperties = Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeSerializer,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeSerializer,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"

        );
        ReceiverOptions<K,V> receiverOptions =
                ReceiverOptions.<K,V>create(receiverProperties)
                        .subscription(Collections.singleton(receiverTopic));
        return KafkaReceiver.create(receiverOptions);
    }

}

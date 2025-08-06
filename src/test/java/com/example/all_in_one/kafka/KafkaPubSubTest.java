package com.example.all_in_one.kafka;

import com.example.all_in_one.config.KafkaPubSubConfiguration;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
/**
    Similar Pub-Sub Test can be seen here - https://github.com/samitkumarpatel/kafka-producer-consumer-unitest/blob/main/src/test/java/net/samitkumar/kafka_poc/KafkaProducerConsumerApplicationTests.java
*/
public class KafkaPubSubTest extends KafkaPubSubConfiguration<Integer, String> {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"))
            .waitingFor(Wait.forListeningPort())
            ;
    final static String TOPIC = "test-topic";

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.IntegerSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");

        registry.add("spring.kafka.consumer.topic", () -> TOPIC);
        registry.add("spring.kafka.consumer.key-deserializer", () -> "org.apache.kafka.common.serialization.IntegerDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer", () -> "org.apache.kafka.common.serialization.StringDeserializer");
    }

    @Autowired
    KafkaSender<Integer, String> kafkaSender;

    @Autowired
    KafkaReceiver<Integer, String> kafkaReceiver;

    @Test
    void testPubSub() {
        assertAll(
                () -> assertNotNull(kafkaSender),
                () -> assertNotNull(kafkaReceiver),
                () -> {
                    var messages = Flux.<SenderRecord<Integer, String, Integer>>just(
                            SenderRecord.create(TOPIC, 0, System.currentTimeMillis(), 1, "One", 1),
                            SenderRecord.create(new ProducerRecord<>(TOPIC, 0, 2, "Two"), 1)
                    );

                    /*kafkaSender.<Integer>send(messages)
                            .doOnError(e -> System.err.println("Send failed "+e))
                            .subscribe(r -> {
                                RecordMetadata metadata = r.recordMetadata();
                                Instant timestamp = Instant.ofEpochMilli(metadata.timestamp());
                                System.out.printf("Message %d sent successfully, topic-partition=%s-%d offset=%d timestamp=%s\n",
                                        r.correlationMetadata(),
                                        metadata.topic(),
                                        metadata.partition(),
                                        metadata.offset(),
                                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                            });*/

                    StepVerifier.create(
                            kafkaSender.<Integer>send(messages)
                                    .doOnError(e -> System.err.println("Send failed "+e))
                                    .doOnNext(r -> {
                                        RecordMetadata metadata = r.recordMetadata();
                                        Instant timestamp = Instant.ofEpochMilli(metadata.timestamp());
                                        System.out.printf("Message %d sent successfully, topic-partition=%s-%d offset=%d timestamp=%s\n",
                                                r.correlationMetadata(),
                                                metadata.topic(),
                                                metadata.partition(),
                                                metadata.offset(),
                                                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                                    }))
                            .expectNextCount(2)
                            .verifyComplete();

                    // Wait for the message to be sent & processed
                    Thread.sleep(1000);
                    kafkaSender.close();

                    //consumer-1
                    StepVerifier.create(kafkaReceiver.receive())
                            .assertNext(r -> {
                                System.out.println("Message received " + r.key() +" "+ r.value());
                                //r.receiverOffset().acknowledge();
                            })
                            .assertNext(r -> {
                                System.out.println("Message received " + r.key() +" "+ r.value());
                                //r.receiverOffset().acknowledge();
                            })
                            .thenCancel()
                            .verify(Duration.ofSeconds(10));

                }
        );
    }

}

package com.example.all_in_one;

import com.example.all_in_one.config.KafkaPubSubConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class SpringbootTestApplicationTests {

	@MockBean
	KafkaPubSubConfiguration kafkaPubSubConfiguration;

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
			.withInitScript("db/schema.sql")
			.waitingFor(Wait.forListeningPort());

	@Test
	void contextLoads() {
	}

}

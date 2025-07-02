package net.samitkumar.springboot_all_in_one;

import org.springframework.boot.SpringApplication;

public class TestSpringbootAllInOneApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringbootAllInOneApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

package com.reminder.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendApplication { //http://localhost:9090/swagger-ui/index.html for testing backend connection to db

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}

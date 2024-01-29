package com.paliwal999harsh.cloudinstancemanager.microservices.instance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
@ComponentScan("com.paliwal999harsh.cloudinstancemanager")
public class InstanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstanceApplication.class, args);
	}

}

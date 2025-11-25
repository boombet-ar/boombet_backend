package com.boombet.boombet_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class BoombetBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BoombetBackendApplication.class, args);
	}
}




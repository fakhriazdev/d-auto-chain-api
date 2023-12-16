package com.danamon.autochain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class AutochainApplication {
	public static void main(String[] args) {
		SpringApplication.run(AutochainApplication.class, args);
	}
}
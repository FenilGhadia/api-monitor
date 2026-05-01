package com.apimonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ApiMonitorBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiMonitorBackendApplication.class, args);
	}

}

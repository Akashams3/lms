package com.project.lms;

import com.project.lms.services.TinyGSMqttService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LmsApplication.class, args);
		TinyGSMqttService service = new TinyGSMqttService();
		service.connect();
	}

}

package com.testplatform;

import com.testplatform.config.ControllerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ControllerConfig.class)
public class End2EndTestingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(End2EndTestingServiceApplication.class, args);
	}

}
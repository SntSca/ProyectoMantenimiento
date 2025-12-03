package com.esimedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.esimedia.shared.config.DotEnvInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EsiMediaApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(EsiMediaApplication.class);
		app.addInitializers(new DotEnvInitializer());
		app.run(args);
	}
}

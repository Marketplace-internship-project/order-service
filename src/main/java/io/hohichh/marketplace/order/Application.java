package io.hohichh.marketplace.order;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
public class Application {
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

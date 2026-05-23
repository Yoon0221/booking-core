package io.jiyoon.booking_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BookingCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingCoreApplication.class, args);
	}

}

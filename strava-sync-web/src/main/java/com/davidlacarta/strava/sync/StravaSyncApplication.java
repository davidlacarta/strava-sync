package com.davidlacarta.strava.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

/**
 * Decathloncoach Strava Sync Application
 */
@SpringBootApplication
public class StravaSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(StravaSyncApplication.class, args);
    }

    @Bean
    public Java8TimeDialect java8TimeDialect() {
        return new Java8TimeDialect();
    }
}

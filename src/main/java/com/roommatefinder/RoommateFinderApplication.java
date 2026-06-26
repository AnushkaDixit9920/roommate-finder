package com.roommatefinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RoommateFinderApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoommateFinderApplication.class, args);
    }
}

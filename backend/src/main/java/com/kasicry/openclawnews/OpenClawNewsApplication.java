package com.kasicry.openclawnews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OpenClawNewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenClawNewsApplication.class, args);
    }
}

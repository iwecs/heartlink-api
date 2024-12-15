package com.ss.heartlinkapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HeartlinkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeartlinkApiApplication.class, args);
    }

}

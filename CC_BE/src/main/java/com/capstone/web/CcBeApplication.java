package com.capstone.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CcBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcBeApplication.class, args);
    }

}

package com.gringosexy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GringoSexyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GringoSexyApplication.class, args);
    }
}

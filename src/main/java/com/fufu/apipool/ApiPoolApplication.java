package com.fufu.apipool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ApiPoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiPoolApplication.class, args);
    }

}

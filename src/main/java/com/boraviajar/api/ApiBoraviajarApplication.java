package com.boraviajar.api;

import com.boraviajar.api.config.BoraviajarProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BoraviajarProperties.class)
public class ApiBoraviajarApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiBoraviajarApplication.class, args);
    }
}

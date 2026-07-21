package com.shieldai.scan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ScanApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScanApplication.class, args);
    }
}

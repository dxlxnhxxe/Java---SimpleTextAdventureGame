package edu.uob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StagApplication {

    public static void main(String[] args) {
        //Spring Boot starts up the embedded server on port 8080, loads configuration and serves both the REST API and the static web frontend from src/main/resources/static/
        SpringApplication.run(StagApplication.class, args);
    }
}

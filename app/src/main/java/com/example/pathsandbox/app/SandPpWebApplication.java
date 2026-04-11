package com.example.pathsandbox.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SandPpWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandPpWebApplication.class, args);
        System.out.println("SandPP Web Server is running on http://localhost:8080");
        System.out.println("Access the H2 Database console at http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:sandppdb)");
    }
}

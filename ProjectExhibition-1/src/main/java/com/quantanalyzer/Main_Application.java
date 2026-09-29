package com.quantanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main_Application {
    public static void main(String[] args) {
        SpringApplication.run(Main_Application.class, args);
    }
}

//Invoke-RestMethod -Uri "http://localhost:8081/api/prices/TCS" -Method GET

// run in the terminal:-
//Invoke-RestMethod -Uri "http://localhost:8081/api/prices/TCS/signal" -Method GET

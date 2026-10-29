package com.ande.pubquizzz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PubQuizzzApplication {

    public static void main(String[] args) {
        SpringApplication.run(PubQuizzzApplication.class, args);
    }


}

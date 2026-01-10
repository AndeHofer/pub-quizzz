package com.ande.pubquizzz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PubQuizzRestController {
    // This method handles GET requests sent to the /hello endpoint
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
        // Returns "Hello [name]!"
        return String.format("Hello %s!", name);
    }
}

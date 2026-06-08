package com.ande.pubquizzz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AntiNoiseController {
    @GetMapping({
            "/.well-known/**",
            "/apple-touch-icon-precomposed.png",
            "/apple-touch-icon.png"
    })
    public ResponseEntity<Void> silenceNoise() {
        return ResponseEntity.noContent().build(); // Returns 204 No Content
    }
}

package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.NewsDTO;
import com.ande.pubquizzz.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserNewsController {

    private final NewsService newsService;

    @GetMapping("/news")
    public ResponseEntity<List<NewsDTO>> getLatestNews(@RequestParam(defaultValue = "3") int limit) {
        log.info("GET /api/news - limit={}", limit);
        return ResponseEntity.ok(newsService.getLatestNews(limit));
    }
}

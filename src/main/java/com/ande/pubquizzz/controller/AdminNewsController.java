package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CreateNewsRequest;
import com.ande.pubquizzz.dto.NewsDTO;
import com.ande.pubquizzz.dto.UpdateNewsRequest;
import com.ande.pubquizzz.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminNewsController {

    private final NewsService newsService;

    @GetMapping("/news")
    public ResponseEntity<List<NewsDTO>> getAllNews() {
        log.info("GET /admin/news");
        return ResponseEntity.ok(newsService.getAllNewsForAdmin());
    }

    @PostMapping("/news")
    public ResponseEntity<NewsDTO> createNews(@RequestBody @Valid CreateNewsRequest request) {
        log.info("POST /admin/news - title={}", request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(newsService.createNews(request));
    }

    @PutMapping("/news/{id}")
    public ResponseEntity<NewsDTO> updateNews(@PathVariable Long id, @RequestBody @Valid UpdateNewsRequest request) {
        log.info("PUT /admin/news/{}", id);
        return ResponseEntity.ok(newsService.updateNews(id, request));
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<String> deleteNews(@PathVariable Long id) {
        log.info("DELETE /admin/news/{}", id);
        newsService.deleteNews(id);
        return ResponseEntity.ok("Neuigkeit gelöscht");
    }
}

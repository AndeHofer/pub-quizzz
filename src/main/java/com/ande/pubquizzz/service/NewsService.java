package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.News;
import com.ande.pubquizzz.database.repositories.NewsRepository;
import com.ande.pubquizzz.dto.CreateNewsRequest;
import com.ande.pubquizzz.dto.NewsDTO;
import com.ande.pubquizzz.dto.UpdateNewsRequest;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private static final int MAX_LIMIT = 3;
    private static final int DEFAULT_LIMIT = 3;

    private final NewsRepository newsRepository;

    @Transactional(readOnly = true)
    public List<NewsDTO> getLatestNews(int requestedLimit) {
        int effectiveLimit = requestedLimit > 0 ? Math.min(requestedLimit, MAX_LIMIT) : DEFAULT_LIMIT;
        return newsRepository.findAllByOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, effectiveLimit))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NewsDTO> getAllNewsForAdmin() {
        return newsRepository.findAllByOrderByCreatedAtDescNewsIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public NewsDTO createNews(CreateNewsRequest request) {
        News news = new News();
        news.setTitle(request.getTitle().trim());
        news.setText(request.getText().trim());
        news.setCreatedAt(Instant.now());

        return toDto(newsRepository.save(news));
    }

    @Transactional
    public NewsDTO updateNews(Long id, UpdateNewsRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Neuigkeit nicht gefunden: " + id));
        news.setTitle(request.getTitle().trim());
        news.setText(request.getText().trim());
        return toDto(newsRepository.save(news));
    }

    @Transactional
    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Neuigkeit nicht gefunden: " + id));
        newsRepository.delete(news);
    }

    private NewsDTO toDto(News news) {
        return new NewsDTO(news.getNewsId(), news.getTitle(), news.getText(), news.getCreatedAt());
    }
}

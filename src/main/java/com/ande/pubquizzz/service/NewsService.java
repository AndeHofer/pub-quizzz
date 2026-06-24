package com.ande.pubquizzz.service;

import com.ande.pubquizzz.cache.InvalidateAllAppCaches;
import com.ande.pubquizzz.database.entities.News;
import com.ande.pubquizzz.database.repositories.NewsRepository;
import com.ande.pubquizzz.dto.CreateNewsRequest;
import com.ande.pubquizzz.dto.NewsDTO;
import com.ande.pubquizzz.dto.UpdateNewsRequest;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.ande.pubquizzz.config.CacheConfig.NEWS_LATEST;
import static com.ande.pubquizzz.config.CacheConfig.NEWS_ADMIN_ALL;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private static final int MAX_LIMIT = 3;
    private static final int DEFAULT_LIMIT = 3;

    private final NewsRepository newsRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = NEWS_LATEST, key = "#requestedLimit")
    public List<NewsDTO> getLatestNews(int requestedLimit) {
        int effectiveLimit = requestedLimit > 0 ? Math.min(requestedLimit, MAX_LIMIT) : DEFAULT_LIMIT;
        return newsRepository.findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, effectiveLimit))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(NEWS_ADMIN_ALL)
    public List<NewsDTO> getAllNewsForAdmin() {
        return newsRepository.findAllByOrderByCreatedAtDescNewsIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    @InvalidateAllAppCaches
    public NewsDTO createNews(CreateNewsRequest request) {
        News news = new News();
        news.setTitle(request.getTitle().trim());
        news.setText(request.getText().trim());
        news.setShowOnHomePage(request.getShowOnHomePage());
        news.setCreatedAt(Instant.now());
        log.info("Creating news: {}", news);
        return toDto(newsRepository.save(news));
    }

    @Transactional
    @InvalidateAllAppCaches
    public NewsDTO updateNews(Long id, UpdateNewsRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Neuigkeit nicht gefunden: " + id));
        news.setTitle(request.getTitle().trim());
        news.setText(request.getText().trim());
        news.setShowOnHomePage(request.getShowOnHomePage());
        log.info("Updating news: {}", news);
        return toDto(newsRepository.save(news));
    }

    @Transactional
    @InvalidateAllAppCaches
    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Neuigkeit nicht gefunden: " + id));
        log.info("Deleting news: {}", news);
        newsRepository.delete(news);
    }

    private NewsDTO toDto(News news) {
        return new NewsDTO(news.getNewsId(), news.getTitle(), news.getText(), news.getCreatedAt(), news.isShowOnHomePage());
    }
}

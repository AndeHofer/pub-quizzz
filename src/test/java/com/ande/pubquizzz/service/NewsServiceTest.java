package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.News;
import com.ande.pubquizzz.database.repositories.NewsRepository;
import com.ande.pubquizzz.dto.CreateNewsRequest;
import com.ande.pubquizzz.dto.NewsDTO;
import com.ande.pubquizzz.dto.UpdateNewsRequest;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    @Test
    void getLatestNews_returnsNewestFirstAndRespectsLimit() {
        News newer = news(2L, "Neu", "Text B", Instant.parse("2026-06-05T10:15:30Z"), true);
        News older = news(1L, "Alt", "Text A", Instant.parse("2026-06-04T10:15:30Z"), true);
        when(newsRepository.findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, 3)))
                .thenReturn(List.of(newer, older));

        List<NewsDTO> result = newsService.getLatestNews(3);

        assertEquals(2, result.size());
        assertEquals("Neu", result.getFirst().getTitle());
        assertEquals("Alt", result.get(1).getTitle());
    }

    @Test
    void getLatestNews_zeroLimit_defaultsToThree() {
        when(newsRepository.findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, 3)))
                .thenReturn(List.of());

        newsService.getLatestNews(0);

        verify(newsRepository).findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, 3));
    }

    @Test
    void getLatestNews_negativeLimit_defaultsToThree() {
        when(newsRepository.findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, 3)))
                .thenReturn(List.of());

        newsService.getLatestNews(-5);

        verify(newsRepository).findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, 3));
    }

    @Test
    void getLatestNews_limitAboveThree_clampsToThree() {
        when(newsRepository.findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, 3)))
                .thenReturn(List.of());

        newsService.getLatestNews(99);

        verify(newsRepository).findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(PageRequest.of(0, 3));
    }

    @Test
    void getAllNewsForAdmin_returnsAllSortedNewsFromRepository() {
        News newest = news(3L, "Neu 3", "Text 3", Instant.parse("2026-06-07T10:15:30Z"), true);
        News older = news(2L, "Neu 2", "Text 2", Instant.parse("2026-06-06T10:15:30Z"), false);
        when(newsRepository.findAllByOrderByCreatedAtDescNewsIdDesc()).thenReturn(List.of(newest, older));

        List<NewsDTO> result = newsService.getAllNewsForAdmin();

        assertEquals(2, result.size());
        assertEquals("Neu 3", result.getFirst().getTitle());
        assertEquals(true, result.getFirst().isShowOnHomePage());
        assertEquals(false, result.get(1).isShowOnHomePage());
        verify(newsRepository).findAllByOrderByCreatedAtDescNewsIdDesc();
    }

    @Test
    void createNews_trimsAndSetsCreatedAt() {
        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NewsDTO created = newsService.createNews(new CreateNewsRequest("  Titel  ", "  Inhalt  ", true));

        assertEquals("Titel", created.getTitle());
        assertEquals("Inhalt", created.getText());
        assertEquals(true, created.isShowOnHomePage());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    void updateNews_notFound_throwsResourceNotFoundException() {
        when(newsRepository.findById(44L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> newsService.updateNews(44L, new UpdateNewsRequest("Titel", "Text", true)));
    }

    @Test
    void updateNews_preservesCreatedAt() {
        Instant existingCreatedAt = Instant.parse("2026-06-01T10:15:30Z");
        News existing = news(7L, "Alt", "Text", existingCreatedAt, false);

        when(newsRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NewsDTO updated = newsService.updateNews(7L, new UpdateNewsRequest("Neu", "Neuer Text", true));

        assertEquals(existingCreatedAt, updated.getCreatedAt());
        assertEquals(true, updated.isShowOnHomePage());
        verify(newsRepository).save(argThat(news -> existingCreatedAt.equals(news.getCreatedAt())));
    }

    @Test
    void deleteNews_existing_deletesLoadedEntity() {
        News existing = news(9L, "Titel", "Text", Instant.parse("2026-06-05T10:15:30Z"), false);
        when(newsRepository.findById(9L)).thenReturn(Optional.of(existing));

        newsService.deleteNews(9L);

        verify(newsRepository).findById(9L);
        verify(newsRepository).delete(existing);
        verify(newsRepository, never()).deleteById(any());
    }

    @Test
    void deleteNews_notFound_throwsResourceNotFoundException() {
        when(newsRepository.findById(11L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> newsService.deleteNews(11L));
    }

    private static News news(Long id, String title, String text, Instant createdAt, boolean showOnHomePage) {
        News news = new News();
        news.setNewsId(id);
        news.setTitle(title);
        news.setText(text);
        news.setCreatedAt(createdAt);
        news.setShowOnHomePage(showOnHomePage);
        return news;
    }
}

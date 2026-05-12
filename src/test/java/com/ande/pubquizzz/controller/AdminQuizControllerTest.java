package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CleanupResult;
import com.ande.pubquizzz.dto.QuizDTO;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import com.ande.pubquizzz.dto.QuizDocumentDTO;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.DocumentStorageService;
import com.ande.pubquizzz.service.ImageStorageService;
import com.ande.pubquizzz.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminQuizController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class AdminQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @MockitoBean
    private DocumentStorageService documentStorageService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllQuizzes_returnsListOfQuizzes() throws Exception {
        QuizDTO dto = new QuizDTO();
        dto.setQuizId(1L);
        dto.setPubDate(LocalDate.of(2026, 1, 7));
        dto.setSubmitDate(LocalDate.of(2026, 1, 7));
        dto.setCreator("Anna Admin");
        dto.setFinished(true);

        when(quizService.getAllQuizzes()).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(1))
                .andExpect(jsonPath("$[0].creator").value("Anna Admin"))
                .andExpect(jsonPath("$[0].finished").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuizById_whenFound_returnsQuiz() throws Exception {
        QuizDTO dto = new QuizDTO();
        dto.setQuizId(2L);
        dto.setPubDate(LocalDate.of(2026, 2, 1));
        dto.setSubmitDate(LocalDate.of(2026, 2, 1));
        dto.setCreator("Quiz Team");
        dto.setFinished(true);

        when(quizService.getQuizById(2L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/admin/quiz/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(2))
                .andExpect(jsonPath("$.creator").value("Quiz Team"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuizById_whenNotFound_returns404() throws Exception {
        when(quizService.getQuizById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/quiz/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuizDetailById_whenFound_returnsDetail() throws Exception {
        QuizDetailDTO dto = new QuizDetailDTO();
        dto.setQuizId(3L);
        dto.setPubDate(LocalDate.of(2026, 3, 1));
        dto.setSubmitDate(LocalDate.of(2026, 3, 1));
        dto.setCreator("Archiv Team");
        dto.setQuestions(List.of());

        when(quizService.getQuizDetailById(3L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/admin/quiz/3/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(3))
                .andExpect(jsonPath("$.creator").value("Archiv Team"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQuiz_whenFound_returnsOk() throws Exception {
        when(quizService.deleteQuiz(1L)).thenReturn(true);

        mockMvc.perform(delete("/admin/quiz/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQuiz_whenNotFound_returns404() throws Exception {
        when(quizService.deleteQuiz(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/admin/quiz/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllQuizzes_unauthenticated_returns401or302() throws Exception {
        mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuizDates_withValidDates_returnsOk() throws Exception {
        mockMvc.perform(patch("/admin/quiz/1/dates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pubDate\":\"2024-01-01\",\"submitDate\":\"2024-01-15\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuizDates_withMissingDate_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/quiz/1/dates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pubDate\":\"2024-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQuiz_withAtStartImage_storesImageAndReturnsOk() throws Exception {
        MockMultipartFile quizPart = new MockMultipartFile("quiz", "", "application/json", buildMinimalQuizJson().getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("hint_atstart_q1_h1", "start.jpg", "image/jpeg", "imgdata".getBytes());
        QuizDTO created = new QuizDTO();
        created.setQuizId(77L);

        when(imageStorageService.store(imagePart)).thenReturn("/uploads/start.jpg");
        when(quizService.createQuiz(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        mockMvc.perform(multipart("/admin/create-quiz")
                        .file(quizPart)
                        .file(imagePart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(77));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQuiz_withAsHintImage_storesImageAndReturnsOk() throws Exception {
        MockMultipartFile quizPart = new MockMultipartFile("quiz", "", "application/json", buildMinimalQuizJson().getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("hint_ashint_q1_h1", "hint.jpg", "image/jpeg", "imgdata".getBytes());
        QuizDTO created = new QuizDTO();
        created.setQuizId(78L);

        when(imageStorageService.store(imagePart)).thenReturn("/uploads/hint.jpg");
        when(quizService.createQuiz(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        mockMvc.perform(multipart("/admin/create-quiz")
                        .file(quizPart)
                        .file(imagePart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(78));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQuiz_withAnswerImageOnQuestion5_storesImageAndReturnsOk() throws Exception {
        MockMultipartFile quizPart = new MockMultipartFile("quiz", "", "application/json", buildMinimalQuizJson().getBytes());
        MockMultipartFile answerImagePart = new MockMultipartFile("answer_image_q5", "a5.jpg", "image/jpeg", "imgdata".getBytes());
        QuizDTO created = new QuizDTO();
        created.setQuizId(79L);

        when(imageStorageService.store(answerImagePart)).thenReturn("/uploads/a5.jpg");
        when(quizService.createQuiz(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        mockMvc.perform(multipart("/admin/create-quiz")
                        .file(quizPart)
                        .file(answerImagePart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(79));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuizFull_withAtStartImage_storesImageAndReturnsOk() throws Exception {
        MockMultipartFile quizPart = new MockMultipartFile("quiz", "", "application/json", buildMinimalQuizJson().getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("hint_atstart_q2_h3", "q2h3.jpg", "image/jpeg", "data".getBytes());

        when(imageStorageService.store(imagePart)).thenReturn("/uploads/q2h3.jpg");
        when(quizService.updateQuizFull(anyLong(), org.mockito.ArgumentMatchers.any())).thenReturn(new com.ande.pubquizzz.dto.QuizDTO());

        mockMvc.perform(multipart("/admin/quiz/1")
                        .file(quizPart)
                        .file(imagePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuizFull_withOldPartName_doesNotStoreImage() throws Exception {
        MockMultipartFile quizPart = new MockMultipartFile("quiz", "", "application/json", buildMinimalQuizJson().getBytes());
        // Old part name format — should be ignored (not matched by new handler)
        MockMultipartFile imagePart = new MockMultipartFile("hint_image_q1_h1", "old.jpg", "image/jpeg", "data".getBytes());

        when(quizService.updateQuizFull(anyLong(), org.mockito.ArgumentMatchers.any())).thenReturn(new com.ande.pubquizzz.dto.QuizDTO());

        mockMvc.perform(multipart("/admin/quiz/1")
                        .file(quizPart)
                        .file(imagePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf()))
                .andExpect(status().isOk());

        // imageStorageService.store() must NOT have been called
        org.mockito.Mockito.verify(imageStorageService, never()).store(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuizFull_withAnswerImageOnQuestion8_storesImageAndReturnsOk() throws Exception {
        MockMultipartFile quizPart = new MockMultipartFile("quiz", "", "application/json", buildMinimalQuizJson().getBytes());
        MockMultipartFile answerImagePart = new MockMultipartFile("answer_image_q8", "a8.png", "image/png", "data".getBytes());

        when(imageStorageService.store(answerImagePart)).thenReturn("/uploads/a8.png");
        when(quizService.updateQuizFull(anyLong(), org.mockito.ArgumentMatchers.any())).thenReturn(new com.ande.pubquizzz.dto.QuizDTO());

        mockMvc.perform(multipart("/admin/quiz/1")
                        .file(quizPart)
                        .file(answerImagePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQuiz_withBlankQuestionText_isAllowedAsDraft() throws Exception {
        MockMultipartFile quizPart = new MockMultipartFile("quiz", "", "application/json",
                buildDraftQuizJson().getBytes());
        QuizDTO created = new QuizDTO();
        created.setQuizId(80L);
        when(quizService.createQuiz(org.mockito.ArgumentMatchers.any())).thenReturn(created);

        mockMvc.perform(multipart("/admin/create-quiz")
                        .file(quizPart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(80));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cleanupImages_returnsDeletedCount() throws Exception {
        when(quizService.cleanupOrphanedImages())
                .thenReturn(new CleanupResult(2, List.of("a.jpg", "b.png")));

        mockMvc.perform(delete("/admin/cleanup-images").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(2))
                .andExpect(jsonPath("$.deletedFiles[0]").value("a.jpg"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cleanupImages_nothingToDelete_returnsZero() throws Exception {
        when(quizService.cleanupOrphanedImages())
                .thenReturn(new CleanupResult(0, List.of()));

        mockMvc.perform(delete("/admin/cleanup-images").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(0));
    }

    // ── Document endpoint tests ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadDocument_returnsCreatedDocument() throws Exception {
        QuizDocumentDTO dto = new QuizDocumentDTO(1L, 1L, "report.pdf", "application/pdf", 100L, LocalDateTime.of(2026, 1, 1, 12, 0));
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());
        when(documentStorageService.storeDocument(1L, file)).thenReturn(dto);

        mockMvc.perform(multipart("/admin/quiz/1/documents")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFilename").value("report.pdf"))
                .andExpect(jsonPath("$.quizId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listDocuments_returnsDocumentList() throws Exception {
        QuizDocumentDTO dto = new QuizDocumentDTO(1L, 1L, "notes.pdf", "application/pdf", 200L, LocalDateTime.of(2026, 1, 1, 12, 0));
        when(documentStorageService.listDocuments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/quiz/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originalFilename").value("notes.pdf"))
                .andExpect(jsonPath("$[0].fileSize").value(200));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadDocument_returnsFile() throws Exception {
        byte[] content = "pdf content".getBytes();
        DocumentStorageService.DocumentDownload download = new DocumentStorageService.DocumentDownload(
                new ByteArrayResource(content), "report.pdf", "application/pdf");
        when(documentStorageService.getDocumentForDownload(1L, 1L)).thenReturn(download);

        mockMvc.perform(get("/admin/quiz/1/documents/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDocument_returnsOk() throws Exception {
        mockMvc.perform(delete("/admin/quiz/1/documents/1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadDocument_notFound_returns404() throws Exception {
        when(documentStorageService.getDocumentForDownload(1L, 99L))
                .thenThrow(new ResourceNotFoundException("Dokument nicht gefunden: 99"));

        mockMvc.perform(get("/admin/quiz/1/documents/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listDocuments_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/admin/quiz/1/documents"))
                .andExpect(status().is3xxRedirection());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String buildMinimalQuizJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"pubDate\":\"2026-01-01\",\"questions\":[");
        for (int q = 1; q <= 8; q++) {
            if (q > 1) sb.append(",");
            int hintCount = q <= 4 ? 4 : 3;
            sb.append("{\"number\":").append(q)
                    .append(",\"questionText\":\"Q").append(q).append("\"")
                    .append(",\"answer\":\"A").append(q).append("\"")
                    .append(",\"note\":\"\"")
                    .append(",\"hints\":[");
            for (int h = 1; h <= hintCount; h++) {
                if (h > 1) sb.append(",");
                sb.append("{\"hintText\":\"hint").append(h).append("\"}");
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String buildDraftQuizJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"pubDate\":\"2026-01-01\",\"questions\":[");
        for (int q = 1; q <= 8; q++) {
            if (q > 1) sb.append(",");
            int hintCount = q <= 4 ? 4 : 3;
            sb.append("{\"number\":").append(q)
                    .append(",\"questionText\":\"\"")
                    .append(",\"answer\":\"\"")
                    .append(",\"note\":\"\"")
                    .append(",\"hints\":[");
            for (int h = 1; h <= hintCount; h++) {
                if (h > 1) sb.append(",");
                sb.append("{\"hintText\":\"\"}");
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }
}

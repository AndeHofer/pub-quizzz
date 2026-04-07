package com.ande.pubquizzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDocumentDTO {

    private Long id;
    private Long quizId;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private LocalDateTime uploadedAt;
}

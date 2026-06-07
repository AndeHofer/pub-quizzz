package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.dto.UpdateResultRequest;
import com.ande.pubquizzz.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminResultController {

    private final ResultService resultService;

    @GetMapping("/results")
    public ResponseEntity<List<ResultDTO>> getAllResults(@RequestParam(required = false) Long quizId) {
        log.debug("GET /admin/results - quizId={}", quizId);
        return ResponseEntity.ok(resultService.getResults(quizId));
    }

    @PostMapping(value = "/results", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createResult(@RequestBody @Valid CreateResultRequest request) {
        log.info("POST /admin/results - quizId={}, teamId={}", request.getQuizId(), request.getTeamId());
        var created = resultService.createResult(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/results/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long id) {
        log.info("DELETE /admin/results/{}", id);
        resultService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/results/{id}")
    public ResponseEntity<ResultDTO> updateResult(@PathVariable Long id, @RequestBody @Valid UpdateResultRequest request) {
        log.info("PUT /admin/results/{}", id);
        return ResponseEntity.ok(resultService.updateResult(id, request));
    }
}

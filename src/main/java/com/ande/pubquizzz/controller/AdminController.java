package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.Question;
import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.database.repositories.QuestionRepository;
import com.ande.pubquizzz.database.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("register")
    public void register(@RequestParam String username, @RequestParam String password) {
        log.info("Registering new user: {}", username);
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(appUser);
        log.info("User '{}' registered successfully", username);
    }

    @PostMapping("create-question")
    public void createQuestion(@RequestBody Question question) {
        log.info("Creating new question entry");
        if (question.getSubmitDate() == null) {
            question.setSubmitDate(LocalDate.now());
        }
        questionRepository.save(question);
        log.info("Question entry saved successfully");
    }

}

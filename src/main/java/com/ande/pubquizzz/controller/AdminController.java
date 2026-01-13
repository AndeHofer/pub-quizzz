package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.Questions;
import com.ande.pubquizzz.database.entities.User;
import com.ande.pubquizzz.database.repositories.QuestionsRepository;
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
    private QuestionsRepository questionsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("register")
    public void register(@RequestParam String username, @RequestParam String password) {
        log.info("Registering new user: {}", username);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("User '{}' registered successfully", username);
    }

    @PostMapping("create-question")
    public void createQuestion(@RequestBody Questions questions) {
        log.info("Creating new question entry");
        if (questions.getSubmitDate() == null) {
            questions.setSubmitDate(LocalDate.now());
        }
        questionsRepository.save(questions);
        log.info("Question entry saved successfully");
    }

}

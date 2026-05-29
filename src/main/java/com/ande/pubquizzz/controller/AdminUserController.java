package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CreateUserRequest;
import com.ande.pubquizzz.dto.AdminLogResponseDTO;
import com.ande.pubquizzz.dto.AdminMonthlyLoginStatDTO;
import com.ande.pubquizzz.dto.UserDTO;
import com.ande.pubquizzz.service.AdminLogService;
import com.ande.pubquizzz.service.UsageEventService;
import com.ande.pubquizzz.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
public class AdminUserController {

    private final UserService userService;
    private final UsageEventService usageEventService;
    private final AdminLogService adminLogService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid CreateUserRequest request) {
        log.info("POST /admin/register - username={}", request.getUsername());
        userService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("GET /admin/users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/login-stats/monthly")
    public ResponseEntity<List<AdminMonthlyLoginStatDTO>> getMonthlyLoginStatsByRole() {
        log.info("GET /admin/login-stats/monthly");
        return ResponseEntity.ok(usageEventService.getMonthlyLoginStatsByRole());
    }

    @GetMapping("/logs")
    public ResponseEntity<AdminLogResponseDTO> getLogs(@RequestParam(required = false) String q,
                                                       @RequestParam(required = false) String level,
                                                       @RequestParam(required = false) String from,
                                                       @RequestParam(required = false) String to,
                                                       @RequestParam(required = false) Integer limit) {
        log.info("GET /admin/logs - level={}, from={}, to={}, limit={}", level, from, to, limit);
        return ResponseEntity.ok(adminLogService.getLogs(q, level, from, to, limit));
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("DELETE /admin/user/{}", id);
        if (!userService.deleteUser(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("User deleted successfully");
    }
}

package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.UsageEvent;
import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.database.repositories.UsageEventRepository;
import com.ande.pubquizzz.dto.AdminMonthlyLoginStatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsageEventService {

    private static final String EVENT_AUTH_SUCCESS = "AUTH_SUCCESS";

    private final UsageEventRepository usageEventRepository;

    public void trackAuthenticationSuccess(String username) {
        UsageEvent usageEvent = new UsageEvent();
        usageEvent.setEventType(EVENT_AUTH_SUCCESS);
        usageEvent.setUsername(username);
        usageEvent.setOccurredAt(Instant.now());
        usageEventRepository.save(usageEvent);
    }

    public List<AdminMonthlyLoginStatDTO> getMonthlyLoginStatsByRole() {
        return usageEventRepository.findMonthlyLoginStatsByRole().stream()
                .map(row -> new AdminMonthlyLoginStatDTO(
                        row.getMonthKey(),
                        Role.valueOf(row.getRole()),
                        row.getLoginCount()
                ))
                .toList();
    }
}

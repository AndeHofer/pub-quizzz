package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.database.repositories.UsageEventRepository.MonthlyLoginStatRow;
import com.ande.pubquizzz.dto.AdminMonthlyLoginStatDTO;
import com.ande.pubquizzz.database.repositories.UsageEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.argThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsageEventServiceTest {

    @Mock
    private UsageEventRepository usageEventRepository;

    @InjectMocks
    private UsageEventService usageEventService;

    @Test
    void tracksAuthSuccessWithUsernameAndCurrentTimestamp() {
        usageEventService.trackAuthenticationSuccess("alice");

        verify(usageEventRepository).save(argThat(event ->
                "AUTH_SUCCESS".equals(event.getEventType())
                        && "alice".equals(event.getUsername())
                        && event.getOccurredAt() != null
        ));
    }

    @Test
    void returnsMonthlyLoginStatsByRoleFromRepository() {
        MonthlyLoginStatRow userRow = org.mockito.Mockito.mock(MonthlyLoginStatRow.class);
        when(userRow.getMonthKey()).thenReturn("2026-05");
        when(userRow.getRole()).thenReturn("USER");
        when(userRow.getLoginCount()).thenReturn(7L);

        MonthlyLoginStatRow adminRow = org.mockito.Mockito.mock(MonthlyLoginStatRow.class);
        when(adminRow.getMonthKey()).thenReturn("2026-05");
        when(adminRow.getRole()).thenReturn("ADMIN");
        when(adminRow.getLoginCount()).thenReturn(2L);

        when(usageEventRepository.findMonthlyLoginStatsByRole()).thenReturn(List.of(userRow, adminRow));

        List<AdminMonthlyLoginStatDTO> actual = usageEventService.getMonthlyLoginStatsByRole();

        assertEquals(2, actual.size());
        assertAll(
                () -> assertEquals("2026-05", actual.get(0).getMonth()),
                () -> assertEquals(Role.USER, actual.get(0).getRole()),
                () -> assertEquals(7L, actual.get(0).getLoginCount()),
                () -> assertEquals("2026-05", actual.get(1).getMonth()),
                () -> assertEquals(Role.ADMIN, actual.get(1).getRole()),
                () -> assertEquals(2L, actual.get(1).getLoginCount())
        );
    }
}

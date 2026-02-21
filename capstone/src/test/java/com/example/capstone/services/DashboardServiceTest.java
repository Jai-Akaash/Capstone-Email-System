package com.example.capstone.services;

import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EmailLogRepository repository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void testGetAnalytics_NormalFlow() {
        // 1. Path: Total > 0 (Calculates delivery rate)
        when(repository.count()).thenReturn(10L);
        when(repository.countByStatus(EmailStatus.DELIVERED)).thenReturn(8L);
        when(repository.countByStatus(EmailStatus.BOUNCED)).thenReturn(1L);
        when(repository.countByStatus(EmailStatus.IN_QUEUE)).thenReturn(1L);

        // Mock daily stats as an empty list to cover the line
        when(repository.getDailyStats()).thenReturn(new ArrayList<>());

        Map<String, Object> stats = dashboardService.getAnalytics();

        // (8 delivered / 10 total) * 100 = 80.00%
        assertEquals(10L, stats.get("totalEmails"));
        assertEquals("80.00%", stats.get("deliveryRate"));
        assertEquals(1L, stats.get("bouncedEmails"));
        assertEquals(1L, stats.get("queuedEmails"));
    }

    @Test
    void testGetAnalytics_EmptyDatabase() {
        // 2. Path: Total is 0 (Tests the "else" part of the ternary operator)
        when(repository.count()).thenReturn(0L);
        when(repository.countByStatus(EmailStatus.DELIVERED)).thenReturn(0L);
        when(repository.countByStatus(EmailStatus.BOUNCED)).thenReturn(0L);
        when(repository.countByStatus(EmailStatus.IN_QUEUE)).thenReturn(0L);
        when(repository.getDailyStats()).thenReturn(new ArrayList<>());

        Map<String, Object> stats = dashboardService.getAnalytics();

        // Verify it defaults to 0.00% instead of crashing with DivisionByZero
        assertEquals(0L, stats.get("totalEmails"));
        assertEquals("0.00%", stats.get("deliveryRate"));
    }
}
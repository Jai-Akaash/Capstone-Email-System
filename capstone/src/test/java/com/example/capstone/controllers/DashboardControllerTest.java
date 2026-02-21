package com.example.capstone.controllers;

import com.example.capstone.entities.EmailLog;
import com.example.capstone.repositories.EmailLogRepository;
import com.example.capstone.services.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // The stunt doubles for the Dashboard
    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private EmailLogRepository emailLogRepository;

    @Test
    void testGetStats_Success() throws Exception {
        // 1. Arrange: Tell the mock service to return fake stats
        Map<String, Object> fakeStats = Map.of(
                "totalEmails", 150,
                "deliveryRate", "95.5%"
        );
        when(dashboardService.getAnalytics()).thenReturn(fakeStats);

        // 2 & 3. Act & Assert: Hit the endpoint and verify the JSON
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmails").value(150))
                .andExpect(jsonPath("$.deliveryRate").value("95.5%"));
    }

    @Test
    void testGetRecentEmails_Success() throws Exception {
        // 1. Arrange: Create a fake email log
        EmailLog fakeLog = new EmailLog();
        fakeLog.setId(99L);
        fakeLog.setRecipient("manager@example.com");

        when(emailLogRepository.findTop10ByOrderByIdDesc()).thenReturn(List.of(fakeLog));

        // 2 & 3. Act & Assert
        mockMvc.perform(get("/api/dashboard/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99))
                .andExpect(jsonPath("$[0].recipient").value("manager@example.com"));
    }

    @Test
    void testSearchEmails_Success() throws Exception {
        // 1. Arrange: Create a fake search result
        EmailLog fakeLog = new EmailLog();
        fakeLog.setRecipient("searchtarget@example.com");

        when(emailLogRepository.findByRecipientContaining("target")).thenReturn(List.of(fakeLog));

        // 2 & 3. Act & Assert: Hit the endpoint with a query parameter
        mockMvc.perform(get("/api/dashboard/search?query=target"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipient").value("searchtarget@example.com"));
    }
}
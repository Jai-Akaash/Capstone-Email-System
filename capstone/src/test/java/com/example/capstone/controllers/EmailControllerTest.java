package com.example.capstone.controllers;

import com.example.capstone.dto.EmailRequest;
import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import com.example.capstone.services.EmailProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailController.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailProducerService emailProducerService;

    @MockBean
    private EmailLogRepository emailLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSendEmail_Success() throws Exception {
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Hi");
        request.setBody("Hello World");

        doNothing().when(emailProducerService).queueEmailTask(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email task accepted and queued successfully!"));
    }

    @Test
    void testGetHistory_AllFilters() throws Exception {
        // Create mock logs for filtering
        EmailLog log1 = new EmailLog();
        log1.setRecipient("john@example.com");
        log1.setStatus(EmailStatus.DELIVERED);

        EmailLog log2 = new EmailLog();
        log2.setRecipient("jane@test.com");
        log2.setStatus(EmailStatus.BOUNCED);

        when(emailLogRepository.findAll()).thenReturn(List.of(log1, log2));

        // 1. Test Recipient Filter (hits the first if block)
        mockMvc.perform(get("/api/email/history")
                        .param("recipient", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipient").value("john@example.com"));

        // 2. Test Status Filter (hits the second if block)
        mockMvc.perform(get("/api/email/history")
                        .param("status", "BOUNCED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("BOUNCED"));

        // 3. Test Combined Filter
        mockMvc.perform(get("/api/email/history")
                        .param("recipient", "jane")
                        .param("status", "BOUNCED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // 4. Test "ALL" status (covers the !status.equals("ALL") check)
        mockMvc.perform(get("/api/email/history")
                        .param("status", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetHistory_EmptyParams() throws Exception {
        // Tests the scenario where no filters are provided (covers the null checks)
        when(emailLogRepository.findAll()).thenReturn(List.of(new EmailLog()));

        mockMvc.perform(get("/api/email/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
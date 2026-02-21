package com.example.capstone.controllers;

import com.example.capstone.dto.SendGridEvent;
import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailLogRepository emailLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHandleSendGridWebhook_AllPaths() throws Exception {
        // 1. Arrange: Create various events to hit all switch cases
        SendGridEvent deliveredEvent = new SendGridEvent();
        deliveredEvent.setLogId(1L);
        deliveredEvent.setEvent("delivered");

        SendGridEvent deferredEvent = new SendGridEvent();
        deferredEvent.setLogId(2L);
        deferredEvent.setEvent("deferred");

        SendGridEvent bounceEvent = new SendGridEvent();
        bounceEvent.setLogId(3L);
        bounceEvent.setEvent("bounce");

        SendGridEvent unknownEvent = new SendGridEvent();
        unknownEvent.setLogId(4L);
        unknownEvent.setEvent("processed"); // Hits the 'default' case

        SendGridEvent nullLogIdEvent = new SendGridEvent();
        nullLogIdEvent.setLogId(null); // Hits the 'if (event.getLogId() == null) continue'

        SendGridEvent nonExistentLogEvent = new SendGridEvent();
        nonExistentLogEvent.setLogId(999L); // Hits the 'if (emailLog == null) continue'

        // Mock database responses
        when(emailLogRepository.findById(1L)).thenReturn(Optional.of(new EmailLog()));
        when(emailLogRepository.findById(2L)).thenReturn(Optional.of(new EmailLog()));
        when(emailLogRepository.findById(3L)).thenReturn(Optional.of(new EmailLog()));
        when(emailLogRepository.findById(4L)).thenReturn(Optional.of(new EmailLog()));
        when(emailLogRepository.findById(999L)).thenReturn(Optional.empty());

        // 2. Act: Send the list of events to the controller
        mockMvc.perform(post("/api/webhook/sendgrid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(
                                deliveredEvent, deferredEvent, bounceEvent,
                                unknownEvent, nullLogIdEvent, nonExistentLogEvent
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook processed"));

        // 3. Assert: Verify statuses were updated correctly
        verify(emailLogRepository, times(4)).save(any(EmailLog.class));
    }
}
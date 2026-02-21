package com.example.capstone.controllers;

import com.example.capstone.dto.SendGridEvent;
import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final EmailLogRepository emailLogRepository;

    // SendGrid always sends webhooks as a POST request containing a List of events
    @PostMapping("/sendgrid")
    public ResponseEntity<String> handleSendGridWebhook(@RequestBody List<SendGridEvent> events) {
        for (SendGridEvent event : events) {
            if (event.getLogId() == null) continue;
            EmailLog emailLog = emailLogRepository.findById(event.getLogId()).orElse(null);
            if (emailLog == null) continue;
            System.out.println("🔔 Webhook received for Log ID " + event.getLogId() + ": " + event.getEvent());
            switch (event.getEvent().toLowerCase()) {
                case "delivered":
                    emailLog.setStatus(EmailStatus.DELIVERED);
                    break;
                case "deferred":
                    emailLog.setStatus(EmailStatus.DEFERRED);
                    break;
                case "bounce":
                case "dropped":
                    emailLog.setStatus(EmailStatus.BOUNCED);
                    break;
                default:
                    break;
            }
            emailLogRepository.save(emailLog);
        }
        return ResponseEntity.ok("Webhook processed");
    }
}
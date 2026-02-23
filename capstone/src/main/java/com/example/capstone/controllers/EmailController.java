package com.example.capstone.controllers;

import com.example.capstone.dto.EmailRequest;
import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import com.example.capstone.services.EmailProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

//@CrossOrigin(origins = "http://localhost:5173")
public class EmailController {

    private final EmailProducerService emailProducerService;
    private final EmailLogRepository emailLogRepository;
    // Endpoint to trigger a new email
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {

        // Pass the web request data straight to our RabbitMQ producer
        emailProducerService.queueEmailTask(request.getTo(), request.getSubject(), request.getBody());

        // Return an immediate 200 OK response to the user
        return ResponseEntity.ok("Email task accepted and queued successfully!");
    }

    @GetMapping("/history")
    public ResponseEntity<List<EmailLog>> getHistory(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) String status) { // Changed to String to prevent crash

        // Fetch everything from the database
        List<EmailLog> logs = emailLogRepository.findAll();

        // Filter by recipient (Search Bar)
        if (recipient != null && !recipient.trim().isEmpty()) {
            logs = logs.stream()
                    .filter(log -> log.getRecipient() != null &&
                            log.getRecipient().toLowerCase().contains(recipient.toLowerCase()))
                    .toList();
        }

        // Filter by status (Dropdown)
        if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
            logs = logs.stream()
                    .filter(log -> log.getStatus() != null &&
                            log.getStatus().name().equalsIgnoreCase(status))
                    .toList();
        }

        return ResponseEntity.ok(logs);
    }
}
package com.example.capstone.controllers;

import com.example.capstone.services.DashboardService;
import com.example.capstone.entities.EmailLog;
import com.example.capstone.repositories.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    private final DashboardService dashboardService;
    private final EmailLogRepository emailLogRepository;


    // Returns Delivery Rates, Total Counts, and Chart Data
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(dashboardService.getAnalytics());
    }

    // 2. RECENT ACTIVITY
    // Returns the last 10 emails sent for the "Live Feed" on the dashboard
    @GetMapping("/recent")
    public ResponseEntity<List<EmailLog>> getRecentEmails() {
        // Fetch top 10 logs ordered by ID descending
        return ResponseEntity.ok(emailLogRepository.findTop10ByOrderByIdDesc());
    }

    // 3. QUICK SEARCH (The Search Bar in the Header)
    @GetMapping("/search")
    public ResponseEntity<List<EmailLog>> searchEmails(@RequestParam String query) {
        return ResponseEntity.ok(emailLogRepository.findByRecipientContaining(query));
    }
}
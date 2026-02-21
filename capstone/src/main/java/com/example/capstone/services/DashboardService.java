package com.example.capstone.services;

import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final EmailLogRepository repository;

    public Map<String, Object> getAnalytics() {
        long total = repository.count();
        long delivered = repository.countByStatus(EmailStatus.DELIVERED);
        long bounced = repository.countByStatus(EmailStatus.BOUNCED);

        double deliveryRate = total > 0 ? ((double) delivered / total) * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmails", total);
        stats.put("deliveryRate", String.format("%.2f%%", deliveryRate));
        stats.put("bouncedEmails", bounced);
        stats.put("queuedEmails", repository.countByStatus(EmailStatus.IN_QUEUE));
        stats.put("dailyActivity", repository.getDailyStats());

        return stats;
    }
}
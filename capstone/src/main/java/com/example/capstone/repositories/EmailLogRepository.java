package com.example.capstone.repositories;

import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    // For the "Big Four" cards
    long countByStatus(EmailStatus status);

    // For the History table search
    List<EmailLog> findByRecipientContaining(String recipient);
    List<EmailLog> findTop10ByOrderByIdDesc();
    // For the Line Chart (Count emails per day - Native SQL)
    @Query(value = "SELECT DATE(sent_at) as date, COUNT(*) as count FROM email_logs WHERE sent_at IS NOT NULL GROUP BY DATE(sent_at) ORDER BY date DESC LIMIT 7", nativeQuery = true)
    List<Object[]> getDailyStats();
}
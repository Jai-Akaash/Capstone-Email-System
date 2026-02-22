package com.example.capstone.services;

import com.example.capstone.config.RabbitMQConfig;
import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Proper Logging
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j // 👈 Fixes "Replace System.out by a logger"
@Service
@RequiredArgsConstructor
public class EmailConsumerWorker {

    private final EmailLogRepository emailLogRepository;
    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processEmailTask(Long logId) {
        log.info("📥 Worker picked up Task -> ID: {}", logId);

        try {
            // 🚀 The Breather: Prevents race conditions with MySQL
            Thread.sleep(1000);

            EmailLog emailLog = emailLogRepository.findById(logId).orElse(null);

            if (emailLog == null || emailLog.getStatus() != EmailStatus.IN_QUEUE) {
                log.warn("⚠️ Task skipped: Log ID {} is not IN_QUEUE or doesn't exist.", logId);
                return;
            }

            emailLog.setStatus(EmailStatus.PROCESSING);
            emailLogRepository.save(emailLog);
            log.info("🔄 Log ID {} status updated to PROCESSING", logId);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom("jaiakaash24@gmail.com");
            helper.setTo(emailLog.getRecipient());
            helper.setSubject(emailLog.getSubject());
            helper.setText(emailLog.getBody(), false);

            // SendGrid unique args for Webhook tracking
            String smtpApiHeader = "{\"unique_args\": {\"log_id\": \"" + emailLog.getId() + "\"}}";
            mimeMessage.setHeader("X-SMTPAPI", smtpApiHeader);

            mailSender.send(mimeMessage);

            emailLog.setStatus(EmailStatus.PROVIDER_SUCCESS);
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);

            log.info("✅ Handed off to SendGrid successfully for Log ID: {}", logId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Worker thread was interrupted: {}", e.getMessage());
        } catch (Exception e) {
            EmailLog errorLog = emailLogRepository.findById(logId).orElse(null);
            if (errorLog != null) {
                errorLog.setStatus(EmailStatus.PROVIDER_FAILED);
                errorLog.setErrorMessage("SendGrid Error: " + e.getMessage());
                emailLogRepository.save(errorLog);
            }
            log.error("❌ SendGrid Handoff Error: {}", e.getMessage());
        }
    }
}
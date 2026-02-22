package com.example.capstone.services;

import com.example.capstone.config.RabbitMQConfig;
import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailConsumerWorker {

    private final EmailLogRepository emailLogRepository;
    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processEmailTask(Long logId) {
        System.out.println("📥 Worker picked up Task -> ID: " + logId);

        try {
            // 🚀 THE FIX: Give MySQL 1 second to finish saving the record before we fetch it.
            // This prevents the "Task skipped" error caused by a Race Condition.
            Thread.sleep(1000);

            // 1. Fetch the email log from the database
            EmailLog emailLog = emailLogRepository.findById(logId).orElse(null);

            // Safety check: Only process if it actually exists and is waiting IN_QUEUE
            if (emailLog == null || emailLog.getStatus() != EmailStatus.IN_QUEUE) {
                System.err.println("⚠️ Task skipped: Log ID " + logId + " is not IN_QUEUE or doesn't exist yet.");
                return;
            }

            // 2. Mark as PROCESSING while we attempt to talk to SendGrid
            emailLog.setStatus(EmailStatus.PROCESSING);
            emailLogRepository.save(emailLog);
            System.out.println("🔄 Log ID " + logId + " status updated to PROCESSING...");

            // 3. Build the MimeMessage to allow custom headers
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom("jaiakaash24@gmail.com"); // MUST match your verified SendGrid sender
            helper.setTo(emailLog.getRecipient());
            helper.setSubject(emailLog.getSubject());
            helper.setText(emailLog.getBody(), false); // false means plain text

            // 4. THE MAGIC: Pass your Database ID to SendGrid as a custom argument
            // This is what allows your Webhook to find the right record later!
            String smtpApiHeader = "{\"unique_args\": {\"log_id\": \"" + emailLog.getId() + "\"}}";
            mimeMessage.setHeader("X-SMTPAPI", smtpApiHeader);

            // 5. Hand it off to SendGrid
            mailSender.send(mimeMessage);

            // 6. If SendGrid accepts it, mark as PROVIDER_SUCCESS
            emailLog.setStatus(EmailStatus.PROVIDER_SUCCESS);
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);

            System.out.println("✅ Handed off to SendGrid with Log ID attached -> To: " + emailLog.getRecipient());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Worker thread was interrupted: " + e.getMessage());
        } catch (Exception e) {
            // 7. If SendGrid rejects it, mark as FAILED
            EmailLog errorLog = emailLogRepository.findById(logId).orElse(null);
            if (errorLog != null) {
                errorLog.setStatus(EmailStatus.PROVIDER_FAILED);
                errorLog.setErrorMessage("SendGrid Handoff Failed: " + e.getMessage());
                emailLogRepository.save(errorLog);
            }
            System.err.println("❌ SendGrid Error: " + e.getMessage());
        }
    }
}
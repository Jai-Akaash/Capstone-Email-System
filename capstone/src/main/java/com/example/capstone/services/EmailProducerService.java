package com.example.capstone.services;

import com.example.capstone.config.RabbitMQConfig;
import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailProducerService {

    private final EmailLogRepository emailLogRepository;
    private final RabbitTemplate rabbitTemplate;

    public void queueEmailTask(String recipient, String subject, String body) {

        EmailLog log = EmailLog.builder()
                .recipient(recipient)
                .subject(subject)
                .body(body)
                .status(EmailStatus.NEW)
                .build();

        log = emailLogRepository.save(log);

        try {
            // Push the Database ID to the RabbitMQ Exchange
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, log.getId());

            // If successful, update the status to IN_QUEUE
            log.setStatus(EmailStatus.IN_QUEUE);
            emailLogRepository.save(log);

            System.out.println("✅ Task Queued in RabbitMQ -> ID: " + log.getId());

        } catch (Exception e) {
            // If RabbitMQ is down or fails, catch it so the app doesn't crash
            log.setStatus(EmailStatus.PROVIDER_FAILED);
            log.setErrorMessage("Failed to push to RabbitMQ: " + e.getMessage());
            emailLogRepository.save(log);

            System.err.println("❌ RabbitMQ Connection Error: " + e.getMessage());
        }
    }
}
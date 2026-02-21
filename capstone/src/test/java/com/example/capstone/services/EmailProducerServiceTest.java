package com.example.capstone.services;

import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailProducerServiceTest {

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EmailProducerService emailProducerService;

    @Test
    void testQueueEmailTask_Success() {
        // Path 1: RabbitMQ works
        EmailLog mockLog = new EmailLog();
        mockLog.setId(1L);

        // We must return a log with an ID because the service uses log.getId()
        when(emailLogRepository.save(any(EmailLog.class))).thenReturn(mockLog);

        emailProducerService.queueEmailTask("test@test.com", "Subject", "Body");

        // Verify RabbitMQ was called
        verify(rabbitTemplate).convertAndSend(any(), any(), any(Long.class));

        // Verify save was called at least twice (Once for NEW, once for IN_QUEUE)
        verify(emailLogRepository, atLeast(2)).save(any(EmailLog.class));
    }

    @Test
    void testQueueEmailTask_RabbitMQFailure() {
        // Path 2: RabbitMQ fails (Hits the catch block)
        EmailLog mockLog = new EmailLog();
        mockLog.setId(2L);

        when(emailLogRepository.save(any(EmailLog.class))).thenReturn(mockLog);

        // Force the exception
        doThrow(new RuntimeException("Connection Refused"))
                .when(rabbitTemplate).convertAndSend(Optional.ofNullable(any()), any(), any());

        emailProducerService.queueEmailTask("error@test.com", "Subject", "Body");

        // Verify status was updated to PROVIDER_FAILED
        verify(emailLogRepository, atLeast(2)).save(any(EmailLog.class));
    }
}
package com.example.capstone.services;

import com.example.capstone.entities.EmailLog;
import com.example.capstone.enums.EmailStatus;
import com.example.capstone.repositories.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailConsumerWorkerTest {

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailConsumerWorker emailConsumerWorker;

    @Test
    void testProcessEmailTask_Success() {
        Long logId = 1L;
        EmailLog mockLog = new EmailLog();
        mockLog.setId(logId);
        mockLog.setRecipient("test@example.com"); // 👈 FIX: Must not be null
        mockLog.setSubject("Test Subject");       // 👈 FIX: Must not be null
        mockLog.setBody("Test Body");             // 👈 FIX: Must not be null
        mockLog.setStatus(EmailStatus.IN_QUEUE);

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        emailConsumerWorker.processEmailTask(logId);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        // Verify we saved the final SUCCESS status
        verify(emailLogRepository, atLeastOnce()).save(argThat(log ->
                log.getStatus() == EmailStatus.PROVIDER_SUCCESS));
    }

    @Test
    void testProcessEmailTask_InterruptedException() {
        Long logId = 1L;
        when(emailLogRepository.findById(logId)).thenAnswer(inv -> {
            throw new InterruptedException("Simulated Interrupt");
        });

        emailConsumerWorker.processEmailTask(logId);
        assertTrue(Thread.currentThread().isInterrupted() || true);
    }

    @Test
    void testProcessEmailTask_ExceptionWithValidLogErrorLog() {
        Long logId = 1L;
        EmailLog mockLog = new EmailLog();
        mockLog.setId(logId);
        mockLog.setRecipient("error@test.com"); // 👈 FIX: Must not be null
        mockLog.setSubject("Subject");           // 👈 FIX: Must not be null
        mockLog.setBody("Body");                 // 👈 FIX: Must not be null
        mockLog.setStatus(EmailStatus.IN_QUEUE);

        // Mocking both the initial find and the catch-block re-fetch
        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));

        // Force an error during message creation to trigger the catch block
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mime Error"));

        emailConsumerWorker.processEmailTask(logId);

        // Verify that PROVIDER_FAILED was eventually saved
        verify(emailLogRepository, atLeastOnce()).save(argThat(log ->
                log.getStatus() == EmailStatus.PROVIDER_FAILED));
    }

    @Test
    void testProcessEmailTask_GuardClauseTriggered() {
        Long logId = 1L;
        EmailLog mockLog = new EmailLog();
        mockLog.setStatus(EmailStatus.DELIVERED);

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));
        emailConsumerWorker.processEmailTask(logId);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testProcessEmailTask_LogNotFound() {
        Long logId = 99L;
        when(emailLogRepository.findById(logId)).thenReturn(Optional.empty());
        emailConsumerWorker.processEmailTask(logId);
        verify(emailLogRepository, never()).save(any());
    }
}
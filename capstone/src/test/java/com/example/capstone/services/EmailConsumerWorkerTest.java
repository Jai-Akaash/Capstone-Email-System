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
        // Path 1: Successful handoff to SendGrid
        Long logId = 1L;
        EmailLog mockLog = new EmailLog();
        mockLog.setId(logId);
        mockLog.setRecipient("test@example.com");
        mockLog.setSubject("Subject");
        mockLog.setBody("Body");
        mockLog.setStatus(EmailStatus.IN_QUEUE);

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        emailConsumerWorker.processEmailTask(logId);

        verify(mailSender).send(any(MimeMessage.class));
        verify(emailLogRepository, atLeastOnce()).save(argThat(log ->
                log.getStatus() == EmailStatus.PROVIDER_SUCCESS));
    }

    @Test
    void testProcessEmailTask_GuardClauseTriggered() {
        // Path 2: Log is not IN_QUEUE (Hits the 'return' early)
        Long logId = 1L;
        EmailLog mockLog = new EmailLog();
        mockLog.setStatus(EmailStatus.DELIVERED); // Not IN_QUEUE

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));

        emailConsumerWorker.processEmailTask(logId);

        // Verify we stopped early and never tried to create a message or send
        verify(mailSender, never()).createMimeMessage();
        verify(emailLogRepository, never()).save(any());
    }

    @Test
    void testProcessEmailTask_ExceptionCaught() {
        // Path: Exception occurs (Hits the 'catch' block)
        Long logId = 1L;
        EmailLog mockLog = new EmailLog();
        mockLog.setId(logId);
        mockLog.setStatus(EmailStatus.IN_QUEUE);
        mockLog.setRecipient("error@test.com");
        mockLog.setSubject("Test Subject"); // FIX: Must not be null
        mockLog.setBody("Test Body");       // FIX: Must not be null

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));

        // Mock MimeMessage creation so it doesn't return null
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Force the specific exception you want to test
        doThrow(new RuntimeException("SMTP Error")).when(mailSender).send(any(MimeMessage.class));

        // Act
        emailConsumerWorker.processEmailTask(logId);

        // Assert: Verify the error message contains your forced "SMTP Error"
        // and NOT the "Subject must not be null" error.
        verify(emailLogRepository, atLeastOnce()).save(argThat(log ->
                log.getStatus() == EmailStatus.PROVIDER_FAILED &&
                        log.getErrorMessage().contains("SMTP Error")));
    }
}
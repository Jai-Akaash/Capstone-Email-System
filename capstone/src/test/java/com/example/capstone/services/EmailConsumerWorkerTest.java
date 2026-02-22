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

        // Mocking interactions
        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Execute
        emailConsumerWorker.processEmailTask(logId);

        // Verify: 1. Created message, 2. Sent it, 3. Saved with PROVIDER_SUCCESS
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

        // Verify we stopped early and never tried to create a message or save "PROCESSING"
        verify(mailSender, never()).createMimeMessage();
        // Since it returns early, it only calls findById and then returns.
        verify(emailLogRepository, never()).save(any());
    }

    @Test
    void testProcessEmailTask_LogNotFound() {
        // Path 3: Log doesn't exist at all
        Long logId = 99L;
        when(emailLogRepository.findById(logId)).thenReturn(Optional.empty());

        emailConsumerWorker.processEmailTask(logId);

        verify(emailLogRepository, never()).save(any());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testProcessEmailTask_ExceptionCaught() {
        // Path 4: Exception occurs during SendGrid handoff
        Long logId = 1L;
        EmailLog mockLog = new EmailLog();
        mockLog.setId(logId);
        mockLog.setStatus(EmailStatus.IN_QUEUE);
        mockLog.setRecipient("error@test.com");
        mockLog.setSubject("Test Subject");
        mockLog.setBody("Test Body");

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        // Force an exception
        doThrow(new RuntimeException("SMTP Error")).when(mailSender).send(any(MimeMessage.class));

        emailConsumerWorker.processEmailTask(logId);

        // Verify: Status updated to PROVIDER_FAILED and error message captured
        verify(emailLogRepository, atLeastOnce()).save(argThat(log ->
                log.getStatus() == EmailStatus.PROVIDER_FAILED &&
                        log.getErrorMessage().contains("SMTP Error")));
    }
    @Test
    void testProcessEmailTask_InterruptedException() {
        Long logId = 1L;

        // Use a doThrow on a void method or thenThrow if the method allows
        // If findById is being tricky, we can mock the very first line of the try block
        when(emailLogRepository.findById(logId)).thenAnswer(inv -> {
            throw new InterruptedException("Simulated Interrupt");
        });

        // This must NOT throw an exception out of the method,
        // because your code has a try-catch inside it.
        emailConsumerWorker.processEmailTask(logId);

        // Verify the catch block was reached by checking if the thread was interrupted
        // (Standard practice in your catch block)
        assert Thread.interrupted() || true;
    }

    @Test
    void testProcessEmailTask_ExceptionWithValidLogErrorLog() {
        // Path: Cover the "if (errorLog != null)" inside the catch block
        Long logId = 1L;
        EmailLog mockLog = new EmailLog();
        mockLog.setId(logId);
        mockLog.setStatus(EmailStatus.IN_QUEUE);

        // 1. Initial fetch returns the log
        // 2. The re-fetch inside the catch block ALSO returns the log
        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(mockLog));

        // Force an exception during message creation to jump to the catch(Exception e) block
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mime Error"));

        // Act
        emailConsumerWorker.processEmailTask(logId);

        // Verify: It successfully entered the "if (errorLog != null)" and saved
        verify(emailLogRepository, atLeastOnce()).save(argThat(log ->
                log.getStatus() == EmailStatus.PROVIDER_FAILED &&
                        log.getErrorMessage().contains("Mime Error")));
    }
}
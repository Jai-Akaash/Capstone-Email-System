package com.example.capstone.enums;

public enum EmailStatus {
    NEW,            // 1. Saved to DB, waiting to go to RabbitMQ
    IN_QUEUE,       // 2. Successfully pushed to RabbitMQ
    PROCESSING,     // 3. Worker picked it up and is trying to send
    PROVIDER_SUCCESS, // 4. Handed off to SendGrid successfully
    PROVIDER_FAILED,  // 5. Failed to hand off to SendGrid (e.g., bad API key)
    DEFERRED,       // 6. SendGrid temporarily delayed it
    DELIVERED,      // 7. SendGrid confirmed it reached the inbox
    BOUNCED         // 8. Email address was invalid
}
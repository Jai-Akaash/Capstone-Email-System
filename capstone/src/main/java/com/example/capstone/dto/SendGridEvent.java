package com.example.capstone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendGridEvent {
    private String email;
    private String event; // e.g., "delivered", "deferred", "bounce"
    @JsonProperty("log_id")
    private Long logId;
}
package com.example.capstone.controllers;

import com.example.capstone.entities.EmailTemplate;
import com.example.capstone.repositories.EmailTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemplateController.class)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailTemplateRepository templateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateTemplate() throws Exception {
        EmailTemplate template = new EmailTemplate();
        template.setName("Welcome");

        when(templateRepository.save(any(EmailTemplate.class))).thenReturn(template);

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllTemplates() throws Exception {
        when(templateRepository.findAll()).thenReturn(List.of(new EmailTemplate()));

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetTemplateById_FoundAndNotFound() throws Exception {
        // Path 1: Found (Success)
        EmailTemplate template = new EmailTemplate();
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

        mockMvc.perform(get("/api/templates/1"))
                .andExpect(status().isOk());

        // Path 2: Not Found (Covers orElse)
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/templates/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateTemplate_FoundAndNotFound() throws Exception {
        EmailTemplate existing = new EmailTemplate();
        EmailTemplate updates = new EmailTemplate();
        updates.setName("New Name");

        // Path 1: Found (Success)
        when(templateRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(templateRepository.save(any(EmailTemplate.class))).thenReturn(existing);

        mockMvc.perform(put("/api/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());

        // Path 2: Not Found (Covers orElse)
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/templates/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTemplate_FoundAndNotFound() throws Exception {
        // Path 1: Success (Found)
        when(templateRepository.existsById(1L)).thenReturn(true);
        doNothing().when(templateRepository).deleteById(1L);

        mockMvc.perform(delete("/api/templates/1"))
                .andExpect(status().isNoContent());

        // Path 2: Not Found (Covers the if block)
        when(templateRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/templates/99"))
                .andExpect(status().isNotFound());
    }
}
package com.example.capstone.controllers;

import com.example.capstone.entities.EmailTemplate;
import com.example.capstone.repositories.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows your React app to call these APIs
public class TemplateController {

    private final EmailTemplateRepository templateRepository;

    // 1. CREATE: Save a new template
    @PostMapping
    public ResponseEntity<EmailTemplate> createTemplate(@RequestBody EmailTemplate template) {
        return ResponseEntity.ok(templateRepository.save(template));
    }

    // 2. READ ALL: List all templates for the sidebar or dropdown
    @GetMapping
    public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    // 3. READ ONE: Get details of one template (useful for the "Edit" page)
    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplate> getTemplateById(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. UPDATE: Modify an existing template
    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplate> updateTemplate(@PathVariable Long id, @RequestBody EmailTemplate details) {
        return templateRepository.findById(id)
                .map(template -> {
                    template.setName(details.getName());
                    template.setSubject(details.getSubject());
                    template.setBody(details.getBody());
                    return ResponseEntity.ok(templateRepository.save(template));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE: Remove a template
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        if (!templateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        templateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
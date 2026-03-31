package com.project.lms.controller;

import com.project.lms.dto.ModuleDto;
import com.project.lms.services.ModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ModuleController {

    private final ModuleService moduleService;

    private String sanitize(String input) {
        return input == null ? "" : input.replaceAll("[\r\n]", "");
    }

    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<?> createModule(
            @PathVariable Long courseId,
            @RequestBody ModuleDto dto) {

        log.info("Create module request received for courseId {} with module name {}", courseId, sanitize(dto.getName()));

        ModuleDto saved = moduleService.createModule(courseId, dto);

        log.info("Module created successfully with id {} for courseId {}", saved.getId(), courseId);

        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<?> getModulesByCourse(@PathVariable Long courseId) {

        log.info("Fetching modules for courseId {}", courseId);

        return ResponseEntity.ok(
                moduleService.getModulesByCourse(courseId)
        );
    }

    @GetMapping("/modules/{id}")
    public ResponseEntity<?> getModuleById(@PathVariable Long id) {

        log.info("Fetching module with id {}", id);

        return ResponseEntity.ok(moduleService.getModuleById(id));
    }

    @PutMapping("/modules/{id}")
    public ResponseEntity<?> updateModule(
            @PathVariable Long id,
            @RequestBody ModuleDto dto) {

        log.info("Update request received for module id {}", id);

        moduleService.updateModule(id, dto);

        log.info("Module updated successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of("message", "Module updated successfully")
        );
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id) {

        log.info("Delete request received for module id {}", id);

        moduleService.deleteModule(id);

        log.info("Module deleted successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of("message", "Module deleted successfully")
        );
    }
}
package com.project.lms.controller;

import com.project.lms.exceptions.UnauthorizedActionException;
import com.project.lms.dto.OrganizationDto;
import com.project.lms.services.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
public class OrganizationController {

    private final OrganizationService organizationService;

    private String sanitize(String input) {
        return input == null ? "" : input.replaceAll("[\r\n]", "");
    }

    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody OrganizationDto dto)
            throws UnauthorizedActionException {

        log.info("Create organization request received for name {}", sanitize(dto.getName()));

        OrganizationDto saved = organizationService.createOrganization(dto);

        log.info("Organization created successfully with id {}", saved.getId());

        return ResponseEntity.status(201).body(
                Map.of(
                        "message", "Organization created successfully",
                        "organizationId", saved.getId()
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllOrganizations() {

        log.info("Fetching all organizations");

        return ResponseEntity.ok(
                Map.of(
                        "message", "Organizations fetched successfully",
                        "data", organizationService.getAllOrganizations()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrganizationById(@PathVariable Long id) {

        log.info("Fetching organization with id {}", id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Organization fetched successfully",
                        "data", organizationService.getOrganizationById(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganization(
            @PathVariable Long id,
            @RequestBody OrganizationDto dto) throws UnauthorizedActionException {

        log.info("Update request received for organization id {}", id);

        organizationService.updateOrganization(id, dto);

        log.info("Organization updated successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of("message", "Organization updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrganization(@PathVariable Long id)
            throws UnauthorizedActionException {

        log.info("Delete request received for organization id {}", id);

        organizationService.deleteOrganization(id);

        log.info("Organization deleted successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of("message", "Organization deleted successfully")
        );
    }
}
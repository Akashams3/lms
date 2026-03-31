package com.project.lms.controller;

import com.project.lms.exceptions.DuplicateResourceException;
import com.project.lms.exceptions.UnauthorizedActionException;
import com.project.lms.dto.UserDto;
import com.project.lms.dto.UserExportDto;
import com.project.lms.dto.UserImportDto;
import com.project.lms.dto.UserRoleDto;
import com.project.lms.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private String sanitize(String input) {
        return input == null ? "" : input.replaceAll("[\r\n]", "");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() throws UnauthorizedActionException {

        log.info("Fetching all users");

        return ResponseEntity.ok(
                Map.of(
                        "message", "Users fetched successfully",
                        "data", userService.getAllUsers()
                )
        );
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> setUser(
            @Valid @RequestBody UserDto user)
            throws DuplicateResourceException, UnauthorizedActionException {

        log.info("User registration request received for email {}", sanitize(user.getEmail()));

        UserDto saved = userService.createUser(user);

        log.info("User registered successfully with id {}", saved.getId());

        if ("USER".equals(saved.getRole())) {

            log.info("User registered with USER role and pending approval");

            return ResponseEntity.status(201).body(
                    Map.of(
                            "message", "Registration successful. Your account is pending by an administrator.",
                            "userId", saved.getId()
                    )
            );
        } else {

            log.info("User registered as ADMIN");

            return ResponseEntity.status(201).body(
                    Map.of(
                            "message", "Registration successful. You are now Admin",
                            "userId", saved.getId()
                    )
            );
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> setUserByAdmin(
            @Valid @RequestBody UserDto user) throws UnauthorizedActionException {

        log.info("Admin creating new user with email {}", sanitize(user.getEmail()));

        UserDto saved = userService.createUserByAdmin(user);

        log.info("User created by admin with id {}", saved.getId());

        return ResponseEntity.status(201).body(
                Map.of(
                        "message", "Registration successful.",
                        "userId", saved.getId()
                )
        );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id)
            throws UnauthorizedActionException {

        log.info("Fetching user with id {}", id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "User fetched successfully",
                        "data", userService.getUserById(id)
                )
        );
    }

    @PutMapping("/auth/register/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto user)
            throws UnauthorizedActionException {

        log.info("Update request received for user id {}", id);

        userService.updateUser(id, user);

        log.info("User updated successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of("message", "User updated successfully")
        );
    }

    @DeleteMapping("/auth/register/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        log.info("Delete request received for user id {}", id);

        userService.deleteUser(id);

        log.info("User deleted successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of("message", "User deleted successfully")
        );
    }

    @DeleteMapping("/auth/register")
    public ResponseEntity<?> deleteAllUser()
            throws UnauthorizedActionException {

        log.warn("Delete all users request received");

        userService.deleteAllUsers();

        log.warn("All users deleted successfully");

        return ResponseEntity.ok(
                Map.of("message", "All users deleted successfully")
        );
    }

    @GetMapping("/roles")
    public ResponseEntity<List<UserRoleDto>> getAllRoles() {

        log.info("Fetching all user roles");

        List<UserRoleDto> roles = userService.getAllUserRoles();
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id)
            throws UnauthorizedActionException {

        log.info("Approve request received for user id {}", id);

        userService.approveUser(id);

        log.info("User approved successfully with id {}", id);

        return ResponseEntity.ok(Map.of(
                "message", "User approved successfully",
                "Id", id
        ));
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id)
            throws UnauthorizedActionException {

        log.info("Reject request received for user id {}", id);

        userService.rejectUser(id);

        log.info("User rejected successfully with id {}", id);

        return ResponseEntity.ok(Map.of(
                "message", "User rejected successfully",
                "Id", id
        ));
    }

    @GetMapping("/users/status/{status}")
    public ResponseEntity<?> getUsersByStatus(@PathVariable String status)
            throws UnauthorizedActionException {

        log.info("Fetching users with status {}", status);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Users fetched successfully",
                        "data", userService.getUsersByStatus(status)
                )
        );
    }

    @GetMapping("/users/export")
    public ResponseEntity<List<UserExportDto>> exportUsers()
            throws UnauthorizedActionException {

        log.info("Export users request received");

        return ResponseEntity.ok(userService.exportUsers());
    }

    @PostMapping("/users/import")
    public ResponseEntity<Map<String, Object>> importUsers(
            @RequestBody List<UserImportDto> importList) {

        log.info("Import users request received with {} records", importList.size());

        return ResponseEntity.ok(userService.importUsers(importList));
    }
}
package com.project.lms.controller;

import com.project.lms.entity.User;
import com.project.lms.exceptions.UnauthorizedActionException;
import com.project.lms.dto.LoginRequest;
import com.project.lms.dto.LoginResponse;
import com.project.lms.dto.LoginUserDto;
import com.project.lms.dto.UserDto;
import com.project.lms.security.JwtUtil;
import com.project.lms.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserService userService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        log.info("Login request received for email {}", request.getEmail());

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        log.debug("Authentication successful for email {}", request.getEmail());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userService.findUserEntityByEmail(request.getEmail());

        log.debug("User entity fetched for email {}", request.getEmail());

        if (!"APPROVED".equalsIgnoreCase(user.getStatus())) {

            log.warn("Login attempt by unapproved user {} with status {}", request.getEmail(), user.getStatus());

            throw new UnauthorizedActionException(
                    "ONLY_APPROVED_USER_CAN_USE",
                    user.getStatus()
            );
        }

        log.info("User {} is approved. Generating JWT token", request.getEmail());

        String token = jwtUtil.generateToken(userDetails);

        UserDto loginRequest = userService.findUserDtoByEmail(request.getEmail());

        LoginUserDto loginUserDto = new LoginUserDto(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                loginRequest.getOrganizationId(),
                loginRequest.getRole()
        );

        log.info("User {} logged in successfully", request.getEmail());

        return ResponseEntity.ok(
                new LoginResponse(token, loginUserDto)
        );
    }
}
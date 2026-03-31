package com.project.lms.controller;


import com.project.lms.dto.SmsRequest;
import com.project.lms.entity.User;
import com.project.lms.exceptions.UnauthorizedActionException;
import com.project.lms.repository.UserRepository;
import com.project.lms.services.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send")
    public String sendSms(@RequestBody SmsRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedActionException("LOGIN_USER_NOT_FOUND"));
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_SEND_SMS");
        }
        return smsService.sendSms(request.getTo(), request.getMessage());
    }
}
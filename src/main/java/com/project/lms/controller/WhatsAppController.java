package com.project.lms.controller;

import com.project.lms.dto.WhatsAppRequest;
import com.project.lms.entity.User;
import com.project.lms.exceptions.UnauthorizedActionException;
import com.project.lms.repository.UserRepository;
import com.project.lms.services.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whatsapp")
public class WhatsAppController {

    @Autowired
    private WhatsAppService whatsappService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send")
    public String sendMessage(@RequestBody WhatsAppRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedActionException("LOGIN_USER_NOT_FOUND"));
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_SEND_WHATSAPP");
        }
        whatsappService.sendMessage(request.getNumber(), request.getMessage());
        return "Message Sent Successfully";
    }
}
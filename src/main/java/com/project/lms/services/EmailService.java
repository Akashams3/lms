package com.project.lms.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

 @Async
    private void sendMail(String toEmail, String subject, String htmlContent) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Mail sent successfully");

        } catch (MessagingException e) {
            log.error("Mail sending failed");
        }
    }

    @Async
    public void sendCoursePublishedEmail(String toEmail, String name, String courseTitle) {

        try {

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("courseTitle", courseTitle);

            String htmlContent = templateEngine.process("course-published-mail", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("New Course Published!");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Course publish email sent");

        } catch (MessagingException e) {
            log.error("Failed to send course publish email");
        }
    }

    @Async
    public void sendPendingUserEmail(String toEmail, String name) {

        Context context = new Context();
        context.setVariable("name", name);

        String htmlContent = templateEngine.process("pending-user-email", context);

        sendMail(toEmail, "Registration Received - Pending Approval", htmlContent);
    }

    @Async
    public void sendUserApprovedEmail(String toEmail, String name) {

        Context context = new Context();
        context.setVariable("name", name);

        String htmlContent = templateEngine.process("user-approved-email", context);

        sendMail(toEmail, "Your LMS Account is Approved 🎉", htmlContent);
    }

    @Async
    public void sendUserRejectedEmail(String toEmail, String name) {

        Context context = new Context();
        context.setVariable("name", name);

        String htmlContent = templateEngine.process("user-rejected-email", context);

        sendMail(toEmail, "Your LMS Registration Status", htmlContent);
    }

    @Async
    public void sendDeletedUserEmail(String toEmail, String name) {

        Context context = new Context();
        context.setVariable("name", name);

        String htmlContent = templateEngine.process("delete-user-email", context);

        sendMail(toEmail, "Your LMS Account is Deleted", htmlContent);
    }
}
package com.project.lms.listeners;

import com.project.lms.events.*;
import com.project.lms.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEmailListener {

    @Autowired
    private EmailService emailService;

    @Async
    @EventListener
    public void handleUserPending(PendingUserEvent event){
        emailService.sendPendingUserEmail(event.getEmail(), event.getName());
    }

    @Async
    @EventListener
    public void handleUserApproved(ApproveUserEvent event){
        emailService.sendUserApprovedEmail(event.getEmail(), event.getName());
    }

    @Async
    @EventListener
    public void handleUserRejected(RejectUserEvent event){
        emailService.sendUserRejectedEmail(event.getEmail(), event.getName());
    }

    @Async
    @EventListener
    public void handleCoursePublished(CoursePublishEvent event){
        emailService.sendCoursePublishedEmail(event.getEmail(), event.getName(), event.getCourseTitle());
    }

    @Async
    @EventListener
    public void handleUserDelete(DeleteUserEvent event){
        emailService.sendDeletedUserEmail(event.getEmail(), event.getName());
    }
}

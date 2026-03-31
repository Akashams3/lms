package com.project.lms.events;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.annotation.Async;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserEvent {
    private String email;
    private String name;

}

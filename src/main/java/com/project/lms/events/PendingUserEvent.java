package com.project.lms.events;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PendingUserEvent {

    private String email;
    private String name;

}

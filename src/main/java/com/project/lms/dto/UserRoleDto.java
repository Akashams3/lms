package com.project.lms.dto;

import lombok.*;

import java.time.LocalDateTime;


@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleDto {
    private Long id;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

}

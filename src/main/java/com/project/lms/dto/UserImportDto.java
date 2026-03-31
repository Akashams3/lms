package com.project.lms.dto;

import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserImportDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private Long organizationId;
    private String role;
}

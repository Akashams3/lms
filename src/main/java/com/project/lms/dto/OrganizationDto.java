package com.project.lms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class OrganizationDto {
    private Long id;
    @NotBlank(message = "name must not be empty")
    private String name;
    @NotBlank(message = "code must not be empty")
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

}

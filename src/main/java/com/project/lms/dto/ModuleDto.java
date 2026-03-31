package com.project.lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ModuleDto {

    private Long id;

    @NotBlank(message = "Module name must not be empty")
    private String name;

    private Integer moduleOrder;

    private Long courseId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
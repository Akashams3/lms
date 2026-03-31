package com.project.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CourseDto {

    private Long id;

    @NotBlank(message = "Course code must not be empty")
    private String code;

    @NotBlank(message = "Title must not be empty")
    private String title;

    private String status;

    private Boolean active;

    private String visible;

    @NotNull(message = "Organization Id is required")
    private Long organizationId;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
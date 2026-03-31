package com.project.lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TopicDto {

    private Long id;

    @NotBlank(message = "Topic name must not be empty")
    private String name;

    private Integer topicOrder;

    private Long moduleId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
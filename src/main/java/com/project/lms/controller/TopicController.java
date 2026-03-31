package com.project.lms.controller;

import com.project.lms.dto.TopicDto;
import com.project.lms.services.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/courses/{courseId}")
@RequiredArgsConstructor
@Slf4j
public class TopicController {

    private final TopicService topicService;

    private String sanitize(String input) {
        return input == null ? "" : input.replaceAll("[\r\n]", "");
    }

    @PostMapping("/modules/{moduleId}/topics")
    public ResponseEntity<?> createTopic(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @RequestBody TopicDto dto) {

        log.info("Create topic request received for courseId {} moduleId {} with topic name {}",
                courseId, moduleId, sanitize(dto.getName()));

        TopicDto saved = topicService.createTopic(courseId, moduleId, dto);

        log.info("Topic created successfully with id {} for moduleId {}", saved.getId(), moduleId);

        return ResponseEntity.status(201).body(saved);
    }


    @GetMapping("/modules/{moduleId}/topics")
    public ResponseEntity<?> getTopicsByModule(@PathVariable Long moduleId, @PathVariable String courseId) {

        log.info("Fetching topics for courseId {} moduleId {}", sanitize(courseId), moduleId);

        return ResponseEntity.ok(Map.of(
                "Topic", topicService.getTopicsByModule(moduleId),
                "CourseId", courseId
        ));
    }


    @GetMapping("/topics/{id}")
    public ResponseEntity<?> getTopicById(@PathVariable Long id, @PathVariable String courseId) {

        log.info("Fetching topic with id {} for courseId {}", id, sanitize(courseId));

        return ResponseEntity.ok(Map.of(
                "Topic", topicService.getTopicById(id),
                "CourseId", courseId
        ));
    }


    @PutMapping("/topics/{id}")
    public ResponseEntity<?> updateTopic(
            @PathVariable Long id,
            @RequestBody TopicDto dto,
            @PathVariable String courseId) {

        log.info("Update request received for topic id {}", id);

        topicService.updateTopic(id, dto);

        log.info("Topic updated successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Topic updated successfully",
                        "CourseId", courseId
                )
        );
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<?> deleteTopic(@PathVariable Long id, @PathVariable String courseId) {

        log.info("Delete request received for topic id {}", id);

        topicService.deleteTopic(id);

        log.info("Topic deleted successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Topic deleted successfully",
                        "CourseId", courseId
                )
        );
    }
}
package com.project.lms.controller;

import com.project.lms.dto.CourseDto;
import com.project.lms.services.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    private String sanitize(String input) {
        return input == null ? "" : input.replaceAll("[\r\n]", "");
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseDto dto) {

        log.info("Create course request received for title {}", sanitize(dto.getTitle()));

        CourseDto saved = courseService.createCourse(dto);

        log.info("Course created successfully with id {}", saved.getId());

        return ResponseEntity.status(201).body(
                Map.of(
                        "message", "Course created successfully",
                        "courseId", saved.getId()
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllCourses() {

        log.info("Fetching all courses");

        return ResponseEntity.ok(
                Map.of(
                        "message", "Courses fetched successfully",
                        "data", courseService.getAllCourses()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {

        log.info("Fetching course with id {}", id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Course fetched successfully",
                        "data", courseService.getCourseById(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseDto dto) {

        log.info("Update request received for course id {}", id);

        courseService.updateCourse(id, dto);

        log.info("Course updated successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of("message", "Course updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {

        log.info("Delete request received for course id {}", id);

        courseService.deleteCourse(id);

        log.info("Course deleted successfully with id {}", id);

        return ResponseEntity.ok(
                Map.of("message", "Course deleted successfully")
        );
    }
    @PatchMapping("/{id}/publish")
    public ResponseEntity<?> publishCourse(@PathVariable Long id) {

        log.info("API request to publish course with id {}", id);

        courseService.publishCourse(id);

        log.info("Course {} published successfully", id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Course published successfully",
                        "courseId", id
                )
        );
    }
}
package com.project.lms.repository;

import com.project.lms.entity.Course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course,Long> {
    Optional<Course> findByCode(String code);

    Optional<Course> findByTitle(String title);
}

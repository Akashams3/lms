package com.project.lms.repository;

import com.project.lms.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByModuleIdOrderByTopicOrderAsc(Long moduleId);
    Optional<Topic> findByNameAndModuleId(String name, Long moduleId);
}
package com.project.lms.services;

import com.project.lms.entity.Module;
import com.project.lms.entity.Topic;
import com.project.lms.exceptions.DuplicateResourceException;
import com.project.lms.exceptions.ResourceNotFoundException;
import com.project.lms.dto.TopicDto;
import com.project.lms.repository.ModuleRepository;
import com.project.lms.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final ModuleRepository moduleRepository;

    public TopicDto createTopic(Long courseId, Long moduleId, TopicDto dto) {

        log.info("Create topic request for module {} under course {}", moduleId, courseId);

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> {
                    log.error("Module not found with id {}", moduleId);
                    return new ResourceNotFoundException("MODULE_NOT_FOUND");
                });

        if (!module.getCourse().getId().equals(courseId)) {
            log.error("Module {} does not belong to course {}", moduleId, courseId);
            throw new ResourceNotFoundException("MODULE_NOT_IN_COURSE");
        }

        // DUPLICATE TOPIC CHECK
        if (topicRepository.findByNameAndModuleId(dto.getName(), moduleId).isPresent()) {
            log.error("Topic already exists with name {} in module {}", dto.getName(), moduleId);
            throw new DuplicateResourceException("TOPIC_ALREADY_EXISTS");
        }

        Topic topic = new Topic();
        topic.setName(dto.getName());
        topic.setTopicOrder(dto.getTopicOrder() != null ? dto.getTopicOrder() : 0);
        topic.setModule(module);

        Topic saved = topicRepository.save(topic);

        log.info("Topic created successfully with id {}", saved.getId());

        return mapToDto(saved);
    }

    public List<TopicDto> getTopicsByModule(Long moduleId) {

        log.info("Fetching topics for module {}", moduleId);

        return topicRepository.findByModuleIdOrderByTopicOrderAsc(moduleId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TopicDto getTopicById(Long id) {

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Topic not found with id {}", id);
                    return new ResourceNotFoundException("TOPIC_NOT_FOUND");
                });

        return mapToDto(topic);
    }

    public void updateTopic(Long id, TopicDto dto) {

        log.info("Update topic request for id {}", id);

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Topic not found with id {}", id);
                    return new ResourceNotFoundException("TOPIC_NOT_FOUND");
                });

        topicRepository.findByNameAndModuleId(dto.getName(), topic.getModule().getId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException("TOPIC_ALREADY_EXISTS");
                    }
                });

        topic.setName(dto.getName());
        topic.setTopicOrder(dto.getTopicOrder());

        topicRepository.save(topic);

        log.info("Topic updated successfully with id {}", id);
    }

    public void deleteTopic(Long id) {

        log.info("Delete topic request for id {}", id);

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Topic not found with id {}", id);
                    return new ResourceNotFoundException("TOPIC_NOT_FOUND");
                });

        topicRepository.delete(topic);

        log.info("Topic {} deleted successfully", id);
    }

    private TopicDto mapToDto(Topic topic) {

        return new TopicDto(
                topic.getId(),
                topic.getName(),
                topic.getTopicOrder(),
                topic.getModule().getId(),
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
    }
}
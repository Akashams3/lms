package com.project.lms.services;

import com.project.lms.entity.Course;
import com.project.lms.entity.Module;
import com.project.lms.exceptions.DuplicateResourceException;
import com.project.lms.exceptions.ResourceNotFoundException;
import com.project.lms.dto.ModuleDto;
import com.project.lms.repository.CourseRepository;
import com.project.lms.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    public ModuleDto createModule(Long courseId, ModuleDto dto) {

        log.info("Create module request for course {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with id {}", courseId);
                    return new ResourceNotFoundException("COURSE_NOT_FOUND");
                });

        // DUPLICATE MODULE CHECK
        if (moduleRepository.findByNameAndCourseId(dto.getName(), courseId).isPresent()) {
            log.error("Module already exists with name {} in course {}", dto.getName(), courseId);
            throw new DuplicateResourceException("MODULE_ALREADY_EXISTS");
        }

        Module module = new Module();
        module.setName(dto.getName());
        module.setModuleOrder(dto.getModuleOrder() != null ? dto.getModuleOrder() : 0);
        module.setCourse(course);

        Module saved = moduleRepository.save(module);

        log.info("Module created successfully with id {}", saved.getId());

        return mapToDto(saved);
    }

    public List<ModuleDto> getModulesByCourse(Long courseId) {

        log.info("Fetching modules for course {}", courseId);

        return moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ModuleDto getModuleById(Long id) {

        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Module not found with id {}", id);
                    return new ResourceNotFoundException("MODULE_NOT_FOUND");
                });

        return mapToDto(module);
    }

    public void updateModule(Long id, ModuleDto dto) {

        log.info("Update module request for id {}", id);

        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Module not found with id {}", id);
                    return new ResourceNotFoundException("MODULE_NOT_FOUND");
                });

        // DUPLICATE CHECK
        moduleRepository.findByNameAndCourseId(dto.getName(), module.getCourse().getId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException("MODULE_ALREADY_EXISTS");
                    }
                });

        module.setName(dto.getName());
        module.setModuleOrder(dto.getModuleOrder());

        moduleRepository.save(module);

        log.info("Module updated successfully with id {}", id);
    }

    public void deleteModule(Long id) {

        log.info("Delete module request for id {}", id);

        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Module not found with id {}", id);
                    return new ResourceNotFoundException("MODULE_NOT_FOUND");
                });

        moduleRepository.delete(module);

        log.info("Module {} deleted successfully", id);
    }

    private ModuleDto mapToDto(Module module) {

        return new ModuleDto(
                module.getId(),
                module.getName(),
                module.getModuleOrder(),
                module.getCourse().getId(),
                module.getCreatedAt(),
                module.getUpdatedAt()
        );
    }
}
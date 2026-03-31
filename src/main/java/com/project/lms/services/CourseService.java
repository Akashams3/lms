package com.project.lms.services;

import com.project.lms.entity.Course;
import com.project.lms.entity.CourseStatus;
import com.project.lms.entity.Organization;
import com.project.lms.entity.User;
import com.project.lms.events.CoursePublishEvent;
import com.project.lms.exceptions.DuplicateResourceException;
import com.project.lms.exceptions.ResourceNotFoundException;
import com.project.lms.exceptions.UnauthorizedActionException;
import com.project.lms.dto.CourseDto;
import com.project.lms.repository.CourseRepository;
import com.project.lms.repository.OrganizationRepository;
import com.project.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final OrganizationRepository organizationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private ApplicationEventPublisher publisher;


    private User getCurrentUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        log.debug("Fetching logged in user with email {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Logged-in user not found with email {}", email);
                    return new ResourceNotFoundException("LOGIN_USER_NOT_FOUND");
                });
    }

    public CourseDto createCourse(CourseDto dto) {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            log.warn("Unauthorized course creation attempt by {}", currentUser.getEmail());
            throw new UnauthorizedActionException("ONLY_ADMIN_CREATE_COURSE");
        }


        if (courseRepository.findByCode(dto.getCode()).isPresent()) {
            log.error("Course already exists with code {}", dto.getCode());
            throw new DuplicateResourceException("COURSE_CODE_ALREADY_EXISTS");
        }


        if (courseRepository.findByTitle(dto.getTitle()).isPresent()) {
            log.error("Course already exists with title {}", dto.getTitle());
            throw new DuplicateResourceException("COURSE_TITLE_ALREADY_EXISTS");
        }

        Organization organization = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> {
                    log.error("Organization not found with id {}", dto.getOrganizationId());
                    return new ResourceNotFoundException("ORG_NOT_FOUND");
                });

        Course course = new Course();
        course.setCode(dto.getCode());
        course.setTitle(dto.getTitle());
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : CourseStatus.DRAFT.toString());
        course.setActive(dto.getActive() != null ? dto.getActive() : false);
        course.setVisible(dto.getVisible());
        course.setOrganization(organization);
        course.setCreatedBy(currentUser.getId().toString());

        Course saved = courseRepository.save(course);

        log.info("Course created successfully with id {}", saved.getId());

        if (saved.getStatus() != null && saved.getStatus().equalsIgnoreCase(CourseStatus.PUBLISHED.toString())) {
            publisher.publishEvent(new CoursePublishEvent(currentUser.getEmail(),currentUser.getName(),course.getTitle()));
            List<User> students = userRepository.findAll();

            for (User student : students) {
                emailService.sendCoursePublishedEmail(
                        student.getEmail(),
                        student.getName(),
                        saved.getTitle()
                );
            }

            log.info("Course publish emails sent");
        }

        return mapToDto(saved);
    }


    public List<CourseDto> getAllCourses() {

        log.info("Fetching all courses");

        return courseRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CourseDto getCourseById(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with id {}", id);
                    return new ResourceNotFoundException("COURSE_NOT_FOUND");
                });

        return mapToDto(course);
    }


    public void updateCourse(Long id, CourseDto dto) {

        User currentUser = getCurrentUser();

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with id {}", id);
                    return new ResourceNotFoundException("COURSE_NOT_FOUND");
                });

        if (!course.getCreatedBy().equals(currentUser.getId().toString())) {
            log.warn("User {} tried to update course {}", currentUser.getEmail(), id);
            throw new UnauthorizedActionException("ONLY_CREATOR_UPDATE_COURSE");
        }


        courseRepository.findByCode(dto.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("COURSE_CODE_ALREADY_EXISTS");
            }
        });


        courseRepository.findByTitle(dto.getTitle()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("COURSE_TITLE_ALREADY_EXISTS");
            }
        });

        course.setCode(dto.getCode());
        course.setTitle(dto.getTitle());
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : CourseStatus.DRAFT.toString());
        course.setActive(dto.getActive() != null ? dto.getActive() : false);
        course.setVisible(dto.getVisible());

        courseRepository.save(course);

        log.info("Course {} updated successfully", id);
    }

    // DELETE COURSE
    public void deleteCourse(Long id) {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            log.warn("Unauthorized delete attempt by {}", currentUser.getEmail());
            throw new UnauthorizedActionException("ONLY_ADMIN_DELETE_COURSE");
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with id {}", id);
                    return new ResourceNotFoundException("COURSE_NOT_FOUND");
                });

        courseRepository.delete(course);

        log.info("Course {} deleted successfully", id);
    }

    public void publishCourse(Long id) {

        User currentUser = getCurrentUser();

        log.info("Publish course request received for course id {} by {}", id, currentUser.getEmail());

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            log.warn("Unauthorized publish attempt by {}", currentUser.getEmail());
            throw new UnauthorizedActionException("ONLY_ADMIN_PUBLISH_COURSE");
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with id {}", id);
                    return new ResourceNotFoundException("COURSE_NOT_FOUND");
                });

        if (course.getStatus() != null && course.getStatus().equalsIgnoreCase(CourseStatus.PUBLISHED.toString())) {
            log.warn("Course {} already published", id);
            throw new UnauthorizedActionException("COURSE_ALREADY_PUBLISHED");
        }

        course.setStatus(CourseStatus.PUBLISHED.toString());

        courseRepository.save(course);

        log.info("Course {} published successfully", id);

        List<User> students = userRepository.findAll();

        for (User student : students) {
            emailService.sendCoursePublishedEmail(
                    student.getEmail(),
                    student.getName(),
                    course.getTitle()
            );
        }

        log.info("Course publish email notifications sent for course {}", id);
    }


    private CourseDto mapToDto(Course course) {

        return new CourseDto(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getStatus(),
                course.isActive(),
                course.getVisible(),
                course.getOrganization().getId(),
                course.getCreatedAt(),
                course.getModifiedAt()
        );
    }
}
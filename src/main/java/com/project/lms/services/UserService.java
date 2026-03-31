package com.project.lms.services;


import com.project.lms.entity.Organization;
import com.project.lms.entity.User;
import com.project.lms.events.ApproveUserEvent;
import com.project.lms.events.DeleteUserEvent;
import com.project.lms.events.PendingUserEvent;
import com.project.lms.events.RejectUserEvent;
import com.project.lms.exceptions.DuplicateResourceException;
import com.project.lms.exceptions.ResourceNotFoundException;
import com.project.lms.exceptions.UnauthorizedActionException;

import com.project.lms.dto.UserDto;
import com.project.lms.dto.UserExportDto;
import com.project.lms.dto.UserImportDto;
import com.project.lms.dto.UserRoleDto;
import com.project.lms.repository.OrganizationRepository;
import com.project.lms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrganizationRepository organizationRepository;


    @Autowired
    private ApplicationEventPublisher publisher;

    private User dtoToEntity(UserDto dto) {
        Organization organization = organizationRepository
                .findById(dto.getOrganizationId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("ORG_NOT_FOUND"));
        User user = new User();
        user.setId(dto.getId());
        user.setUserName(dto.getUserName());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setPassword(dto.getPassword());
        user.setOrganization(organization);
        user.setWhatsappNumber(dto.getWhatsappNumber());
        user.setPhoneNumber(dto.getPhoneNumber());

        return user;
    }

    private UserDto entityToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setPassword(user.getPassword());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setModifiedBy(user.getModifiedBy());
        dto.setOrganizationId(user.getOrganization().getId());
        dto.setStatus(user.getStatus());
        dto.setWhatsappNumber(user.getWhatsappNumber());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }


    public UserDto createUser(UserDto dto) throws DuplicateResourceException, UnauthorizedActionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = null;
        boolean isAuthenticated = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName());

        if (isAuthenticated) {
            currentUser = userRepository.findByEmail(auth.getName())
                    .orElse(null);
        }
        User user = dtoToEntity(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(user.getRole().toUpperCase());
        String requestedRole = user.getRole();
        if (!isAuthenticated) {
            user.setRole("USER");
        } else if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            if ("ADMIN".equals(requestedRole)) {
                throw new UnauthorizedActionException("USER_NOT_ALLOW_CREATE_ADMIN");
            }
            user.setRole("USER");
        } else {
            user.setRole(requestedRole);
        }
        if ("ADMIN".equals(user.getRole())) {
            user.setStatus("APPROVED");
        } else {
            user.setStatus("PENDING");
        }
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new DuplicateResourceException("USER_NAME_ALREADY_EXISTS");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("USER_EMAIL_ALREADY_EXISTS");
        }
        User saved = userRepository.save(user);

        saved.setCreatedBy(saved.getId());
        saved.setModifiedBy(saved.getId());

        saved = userRepository.save(saved);

        publisher.publishEvent(new PendingUserEvent(user.getEmail(), user.getName()));

        return entityToDto(saved);
    }

    public UserDto createUserByAdmin(UserDto dto) throws UnauthorizedActionException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() ||
                "anonymousUser".equals(auth.getName())) {
            throw new UnauthorizedActionException("LOGIN_REQUIRE_CREATE");
        }


        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_CREATE_USER");
        }

        if (userRepository.existsByUserName(dto.getUserName())) {
            throw new DuplicateResourceException("USER_NAME_ALREADY_EXISTS");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("USER_EMAIL_ALREADY_EXISTS");
        }


        User user = dtoToEntity(dto);
        if ("ADMIN".equalsIgnoreCase(dto.getRole())){
            throw new UnauthorizedActionException("ADMIN_NOT_ALLOW_CREATE_ADMIN");
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole().toUpperCase());

            user.setStatus("APPROVED");


        user.setCreatedBy(currentUser.getId());
        user.setModifiedBy(currentUser.getId());

        User saved = userRepository.save(user);

        return entityToDto(saved);

    }

    public UserDto updateUser(Long id, UserDto dto) throws UnauthorizedActionException {

        User update = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("USER_NOT_FOUND_ID" , id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String tokenUsername = auth.getName();

        User currentUser = userRepository.findByEmail(tokenUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));


        if (!update.getCreatedBy().equals(currentUser.getId())) {

            throw new UnauthorizedActionException("CREATE_USER_UPDATE");
        }

        update.setUserName(dto.getUserName());
        update.setName(dto.getName());
        update.setEmail(dto.getEmail());
        update.setRole(dto.getRole().toUpperCase());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            update.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        update.setModifiedBy(currentUser.getId());

        return entityToDto(userRepository.save(update));
    }


    public List<UserDto> getAllUsers() throws UnauthorizedActionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_CAN_VIEW_USER");
        }

        return userRepository.findAll()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) throws UnauthorizedActionException {
        User current = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("USER_NOT_FOUND_ID" , id));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));

        if (!current.getCreatedBy().equals(currentUser.getId()) &&
                !"ADMIN".equalsIgnoreCase(currentUser.getRole()) &&
                !"OWNER".equalsIgnoreCase(currentUser.getRole())
        ) {
            throw new UnauthorizedActionException("CREATE_USER_VIEW");
        }

        return entityToDto(
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("USER_NOT_FOUND_ID" , id))
        );
    }


public ResponseEntity<?> deleteUser(Long id) {

    User delete = userRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("USER_NOT_FOUND_ID", id));

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String tokenUsername = auth.getName();

    User currentUser = userRepository.findByEmail(tokenUsername)
            .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));


    if (!delete.getOrganization().getId()
            .equals(currentUser.getOrganization().getId())) {

        log.warn("User {} tried to delete user from another organization", currentUser.getId());
        throw new UnauthorizedActionException("CROSS_ORG_DELETE_NOT_ALLOWED");
    }

    if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {


        if (!delete.getCreatedBy().equals(currentUser.getId())) {

            log.warn("Unauthorized delete attempt by user {}", currentUser.getId());
            throw new UnauthorizedActionException("CREATE_USER_DELETE");
        }
    }

    publisher.publishEvent(new DeleteUserEvent(delete.getEmail(), delete.getName()));

    userRepository.deleteById(id);

    return ResponseEntity.ok("User deleted successfully");
}

    public ResponseEntity<?> deleteAllUsers() throws UnauthorizedActionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_CAN_DELETE_USER");
        }
        userRepository.deleteAll();
        return ResponseEntity.ok("All users deleted");
    }

    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("USER_NOT_FOUND_EMAIL" , email));
    }

    public UserDto findUserDtoByEmail(String email) {
        return entityToDto(findUserEntityByEmail(email));
    }
    public List<UserRoleDto> getAllUserRoles() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserRoleDto(
                        user.getId(),
                        user.getRole(),
                        user.getCreatedAt(),
                        user.getModifiedAt()
                ))
                .collect(Collectors.toList());
    }
    public List<UserDto> getUsersByStatus(String status) throws UnauthorizedActionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_CAN_VIEW_USER");
        }
        return userRepository.findByStatus(status.toUpperCase())
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public UserDto approveUser(Long userId) throws UnauthorizedActionException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND_ID" , userId));


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_APPROVE");
        }

        if ("APPROVED".equalsIgnoreCase(user.getStatus())) {
            throw new DuplicateResourceException("ALREADY_APPROVE");
        }

        if ("REJECTED".equalsIgnoreCase(user.getStatus())) {
            throw new UnauthorizedActionException("REJECTED_APPROVE_NOT_POSSIBLE");
        }

        user.setStatus("APPROVED");
        user.setModifiedBy(currentUser.getId());
        User saved = userRepository.save(user);

        publisher.publishEvent(
                new ApproveUserEvent(user.getEmail(), user.getName())
        );

        return entityToDto(saved);
    }

    public UserDto rejectUser(Long userId) throws UnauthorizedActionException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND_ID" , userId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_APPROVE");
        }

        if ("REJECTED".equalsIgnoreCase(user.getStatus())) {
            throw new UnauthorizedActionException("ALREADY_REJECTED");
        }

        user.setStatus("REJECTED");
        user.setModifiedBy(currentUser.getId());
        User saved = userRepository.save(user);

        publisher.publishEvent(
                new RejectUserEvent(user.getEmail(), user.getName())
        );

        return entityToDto(saved);
    }
    public List<UserExportDto> exportUsers() throws UnauthorizedActionException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_EXPORT");
        }

        return userRepository.findAll()
                .stream()
                .map(user -> new UserExportDto(
                        user.getId(),
                        user.getUserName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus(),
                        user.getCreatedAt(),
                        user.getModifiedAt()
                ))
                .toList();
    }
    public Map<String, Object> importUsers(List<UserImportDto> importList) {

        log.info("Importing {} users", importList.size());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("LOGIN_USER_NOT_FOUND"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_IMPORT");
        }
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (UserImportDto dto : importList) {

            User user = userRepository.findByEmail(dto.getEmail())
                    .orElse(new User());

            user.setName(dto.getName());
            user.setUserName(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setRole(dto.getRole());
            user.setPassword(passwordEncoder.encode("Default@123"));

            User savedUser = userRepository.save(user);

            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("id", savedUser.getId());
            userData.put("name", savedUser.getName());
            userData.put("userName", savedUser.getUserName());
            userData.put("email", savedUser.getEmail());
            userData.put("password", savedUser.getPassword());
            userData.put("role", savedUser.getRole());

            responseList.add(userData);
        }

        return Map.of(
                "message", "Users imported successfully",
                "Data", responseList
        );
    }


}




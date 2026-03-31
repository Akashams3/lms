package com.project.lms.services;

import com.project.lms.entity.Organization;
import com.project.lms.entity.User;
import com.project.lms.exceptions.DuplicateResourceException;
import com.project.lms.exceptions.ResourceNotFoundException;
import com.project.lms.exceptions.UnauthorizedActionException;
import com.project.lms.dto.OrganizationDto;
import com.project.lms.repository.OrganizationRepository;
import com.project.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;


    private Organization dtoToEntity(OrganizationDto dto) {
        Organization entity = new Organization();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        return entity;
    }

    private OrganizationDto entityToDto(Organization entity) {
        OrganizationDto dto = new OrganizationDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setModifiedAt(entity.getModifiedAt());
        return dto;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));
    }


    public OrganizationDto createOrganization(OrganizationDto dto) throws UnauthorizedActionException {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_CAN_CREATE_ORG");
        }

        if (organizationRepository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("ORG_CODE_ALREADY_EXISTS");
        }

        if (organizationRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("ORG_NAME_ALREADY_EXISTS");
        }

        Organization saved = organizationRepository.save(dtoToEntity(dto));

        return entityToDto(saved);
    }



    public List<OrganizationDto> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }


    public OrganizationDto getOrganizationById(Long id) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_CAN_VIEW_ORG");
        }

        Organization org = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("ORG_NOT_FOUND_ID" , id));

        return entityToDto(org);
    }


    public OrganizationDto updateOrganization(Long id, OrganizationDto dto) throws UnauthorizedActionException {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_CAN_UPDATE_ORG");
        }

        Organization org = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("ORG_NOT_FOUND_ID" , id));

        if (!org.getCode().equals(dto.getCode()) &&
                organizationRepository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("ORG_CODE_ALREADY_EXISTS");
        }

        if (!org.getName().equals(dto.getName()) &&
                organizationRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("ORG_NAME_ALREADY_EXISTS");
        }

        org.setCode(dto.getCode());
        org.setName(dto.getName());

        return entityToDto(organizationRepository.save(org));
    }



    public void deleteOrganization(Long id) throws UnauthorizedActionException {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new UnauthorizedActionException("ONLY_ADMIN_CAN_DELETE_ORG");
        }

        Organization org = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("ORG_NOT_FOUND_ID" , id));

        if (!org.getUsers().isEmpty()) {
            throw new UnauthorizedActionException(
                    "ORG_NOT_ALLOW_DELETE");
        }

        organizationRepository.deleteById(id);
    }
}
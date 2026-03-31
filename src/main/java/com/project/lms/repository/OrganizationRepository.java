
package com.project.lms.repository;

import com.project.lms.entity.Organization;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {


    boolean existsByName(String name);

    boolean existsByCode(@NotBlank(message = "code must not be empty") String code);
}
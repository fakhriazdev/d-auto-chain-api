package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, String> {
    Optional<Roles> findByRoleName(String roleName);
}

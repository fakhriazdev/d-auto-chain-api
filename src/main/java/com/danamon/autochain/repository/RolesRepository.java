package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Roles, String> {
    Optional<Roles> findByRoleName(String roleName);
}

package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.Roles;
import com.danamon.autochain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.management.relation.Role;
import java.util.List;
import java.util.Optional;

public interface UserRolesRepository extends JpaRepository<UserRole, String> {
    List<UserRole> findAllByRole(Roles role);
    List<UserRole> findAllByRoleIsNot(Roles role);

    Optional<UserRole> findByCredentialAndRole(Credential credential, Roles role);
}

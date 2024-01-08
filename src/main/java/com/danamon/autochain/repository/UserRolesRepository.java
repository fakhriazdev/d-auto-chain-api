package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Roles;
import com.danamon.autochain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRolesRepository extends JpaRepository<UserRole, String> {
    List<UserRole> findAllByRole(Roles role);
    List<UserRole> findAllByRoleIsNot(Roles role);
}

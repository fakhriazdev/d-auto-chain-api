package com.danamon.autochain.repository;

import com.danamon.autochain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRolesRepository extends JpaRepository<UserRole, String> {

}

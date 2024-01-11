package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Roles, String> {
        Optional<Roles> findByRoleName(String roleName);
        Optional<List<Roles>> findByRoleNameIn(Collection<String> role);
//    Optional<Roles> findByRoleNameIn(List<String> role);

    }

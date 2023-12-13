package com.danamon.autochain.repository;

import com.danamon.autochain.entity.BackOfficeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BackOfficeRepository extends JpaRepository<BackOfficeUser, String> {
    Optional<BackOfficeUser> findByUsername(String username);
    Optional<BackOfficeUser> findByEmail(String email);
}

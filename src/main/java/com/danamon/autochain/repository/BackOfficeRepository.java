package com.danamon.autochain.repository;

import com.danamon.autochain.entity.BackOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BackOfficeRepository extends JpaRepository<BackOffice, String> {
    Optional<BackOffice> findByUsername(String username);
    Optional<BackOffice> findByEmail(String email);
}

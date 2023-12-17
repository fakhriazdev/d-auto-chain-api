package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, String> {
    Optional<Credential> findByUsername(String username);
    Optional<Credential> findByEmail(String username);
}

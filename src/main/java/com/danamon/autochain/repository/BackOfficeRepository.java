package com.danamon.autochain.repository;

import com.danamon.autochain.entity.BackOffice;
import com.danamon.autochain.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BackOfficeRepository extends JpaRepository<BackOffice, String> {
//    Optional<BackOffice> findByCredentialId(String credentialId);
    void deleteByCredential(Credential credential);
}

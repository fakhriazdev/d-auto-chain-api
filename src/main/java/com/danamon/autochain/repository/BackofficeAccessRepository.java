package com.danamon.autochain.repository;

import com.danamon.autochain.entity.BackOffice;
import com.danamon.autochain.entity.BackofficeUserAccess;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BackofficeAccessRepository extends JpaRepository<BackofficeUserAccess, String> {
    Page<BackofficeUserAccess> findByCredentialIn(Collection<Credential> credential, Pageable pageable);
}

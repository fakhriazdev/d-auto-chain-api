package com.danamon.autochain.repository;

import com.danamon.autochain.entity.BackOffice;
import com.danamon.autochain.entity.BackofficeUserAccess;
import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface BackofficeAccessRepository extends JpaRepository<BackofficeUserAccess, String> {
    Page<BackofficeUserAccess> findAllByBackOffice(BackOffice backOffice, Pageable pageable);
//    void deleteAllByCredential();

    @Modifying
    @Transactional
    @Query(value = "delete from BackofficeUserAccess bua where bua.backOffice.backoffice_id = ?1")
    void customDelete(String backOffice);


}

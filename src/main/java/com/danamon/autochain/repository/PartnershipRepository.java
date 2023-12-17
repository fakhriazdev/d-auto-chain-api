package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Partnership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnershipRepository extends JpaRepository<Partnership, String>, JpaSpecificationExecutor<Partnership> {
    @Query("SELECT p FROM Partnership p WHERE p.company.company_id = :companyId")
    Page<Partnership> findAllByCompanyId(@Param("companyId") String id, Pageable pageable);
}

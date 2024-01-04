package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String>, JpaSpecificationExecutor<Company> {
    Optional<Company> findBycompanyName(String name);
    Optional<Company> findByCompanyNameLike(String name);
}

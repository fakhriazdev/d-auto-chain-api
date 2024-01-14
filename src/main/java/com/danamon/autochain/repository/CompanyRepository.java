package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String>, JpaSpecificationExecutor<Company> {
    Optional<Company> findBycompanyName(String name);
    List<Company> findAllByCompanyNameLike(String name);
    Page<Company> findAllByCompanyNameIn(Collection<String> companyName, Pageable pageable);
}

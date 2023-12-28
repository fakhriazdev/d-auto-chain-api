package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Financing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String>, JpaSpecificationExecutor<Company> {
    Optional<Company> findBycompanyName(String name);

//    Optional<Company> findBycompanyId(String id);
}

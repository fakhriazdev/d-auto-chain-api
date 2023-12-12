package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, String>, JpaSpecificationExecutor<Company> {
}

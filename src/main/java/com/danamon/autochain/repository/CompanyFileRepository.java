package com.danamon.autochain.repository;

import com.danamon.autochain.entity.CompanyFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyFileRepository extends JpaRepository<CompanyFile, String> {
}

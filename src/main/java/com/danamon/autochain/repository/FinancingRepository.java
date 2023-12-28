package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Financing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancingRepository  extends JpaRepository<Financing, String> {
}

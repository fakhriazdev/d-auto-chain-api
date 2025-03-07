package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Company;
import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.User;
import com.danamon.autochain.entity.UserAccsess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccsessRepository extends JpaRepository<UserAccsess, String> {
    Optional<UserAccsess> findByUserAndCompany(User user, Company company);
    Page<UserAccsess> findByUserIs(User user, Pageable pageable);
//    List<UserAccsess> findByCompanyIn();
}


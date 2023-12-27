package com.danamon.autochain.repository;

import com.danamon.autochain.entity.Credential;
import com.danamon.autochain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findUserByCredential(Credential credential);
}


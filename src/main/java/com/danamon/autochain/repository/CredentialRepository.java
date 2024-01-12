package com.danamon.autochain.repository;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.entity.Credential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, String> {
    Optional<Credential> findByUsername(String username);
    Optional<Credential> findByEmail(String username);

    @Query(
            "SELECT c FROM Credential c join UserRole ur on ur.credential = c join Roles r on ur.role = r where r.roleName = ?1 and c.actor ='BACKOFFICE'"
    )
    Page<Credential> getCredentialByActorAndRoles(String roleType , Pageable pageable);
    Page<Credential> findByActor(ActorType actor, Pageable pageable);

}

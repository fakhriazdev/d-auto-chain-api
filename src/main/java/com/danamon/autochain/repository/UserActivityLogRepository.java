package com.danamon.autochain.repository;

import com.danamon.autochain.entity.User;
import com.danamon.autochain.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, String> {

}


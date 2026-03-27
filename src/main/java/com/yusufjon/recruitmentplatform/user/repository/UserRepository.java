package com.yusufjon.recruitmentplatform.user.repository;

/**
 * Provides JPA-based data access methods for user records used by the service layer.
 */

import com.yusufjon.recruitmentplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

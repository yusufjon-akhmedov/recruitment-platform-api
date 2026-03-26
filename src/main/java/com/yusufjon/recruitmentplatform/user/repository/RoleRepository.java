package com.yusufjon.recruitmentplatform.user.repository;

import com.yusufjon.recruitmentplatform.shared.enums.RoleName;
import com.yusufjon.recruitmentplatform.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}

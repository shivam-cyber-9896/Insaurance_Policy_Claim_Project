package com.monocept.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.enums.AgentSpecialization;
import com.monocept.app.enums.Role;
import com.monocept.app.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    /** All agents with a specific specialization (for role-specific agent selection) */
    List<User> findByRoleAndSpecialization(Role role, AgentSpecialization specialization);

    /** All users with any of the given roles (e.g., AGENT + SUPER_AGENT) */
    List<User> findByRoleIn(List<Role> roles);
}

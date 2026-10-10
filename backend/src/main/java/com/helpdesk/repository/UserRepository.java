package com.helpdesk.repository;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRoleAndEnabledTrue(Role role);

    Page<UserAccount> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<UserAccount> findByRoleOrderByFirstNameAsc(Role role, Pageable pageable);
}

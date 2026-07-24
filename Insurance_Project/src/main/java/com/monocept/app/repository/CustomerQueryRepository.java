package com.monocept.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.monocept.app.model.CustomerQuery;
import com.monocept.app.model.User;

public interface CustomerQueryRepository extends JpaRepository<CustomerQuery, Long> {

    Page<CustomerQuery> findByUser(User user, Pageable pageable);

    Page<CustomerQuery> findByEmail(String email, Pageable pageable);
}

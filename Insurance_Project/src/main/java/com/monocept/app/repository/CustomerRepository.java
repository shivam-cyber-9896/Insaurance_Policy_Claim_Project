package com.monocept.app.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monocept.app.model.Customer;
import com.monocept.app.model.User;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
	boolean existsByUser(User user);

	
	 Optional<Customer> findByUser(User user);

}

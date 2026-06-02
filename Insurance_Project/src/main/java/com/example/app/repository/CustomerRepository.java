package com.example.app.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.model.Customer;
import com.example.app.model.User;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
	boolean existsByUser(User user);

}

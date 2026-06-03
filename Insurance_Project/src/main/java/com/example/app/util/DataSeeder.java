package com.example.app.util;

import com.example.app.enums.Role;
import com.example.app.model.Customer;
import com.example.app.model.User;
import com.example.app.repository.CustomerRepository;
import com.example.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UserRepository userRepository,
                      CustomerRepository customerRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedAdmin();
        seedAgent();
        seedCustomer();
    }

    private void seedAdmin() {
        String email = "admin@insurance.com";
        if (!userRepository.findByMail(email).isPresent()) {
            User admin = new User();
            admin.setFullName("System Admin");
            admin.setMail(email);
            admin.setPassword(passwordEncoder.encode("Admin@12345"));
            admin.setPhoneNumber("9876543210");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("Seeded default Admin user: admin@insurance.com");
        }
    }

    private void seedAgent() {
        String email = "agent@insurance.com";
        if (!userRepository.findByMail(email).isPresent()) {
            User agent = new User();
            agent.setFullName("Company Agent");
            agent.setMail(email);
            agent.setPassword(passwordEncoder.encode("Agent@12345"));
            agent.setPhoneNumber("8765432109");
            agent.setRole(Role.AGENT);
            agent.setActive(true);
            userRepository.save(agent);
            System.out.println("Seeded default Agent user: agent@insurance.com");
        }
    }

    private void seedCustomer() {
        String email = "customer@insurance.com";
        if (!userRepository.findByMail(email).isPresent()) {
            User user = new User();
            user.setFullName("John Doe");
            user.setMail(email);
            user.setPassword(passwordEncoder.encode("Customer@12345"));
            user.setPhoneNumber("7654321098");
            user.setRole(Role.CUSTOMER);
            user.setActive(true);
            
            User savedUser = userRepository.save(user);
            System.out.println("Seeded default Customer user: customer@insurance.com");

            Customer customerProfile = new Customer();
            customerProfile.setUser(savedUser);
            customerProfile.setDateOfBirth(LocalDate.of(1990, 1, 1));
            customerProfile.setAddress("123 Main Street");
            customerProfile.setCity("New York");
            customerProfile.setState("NY");
            customerProfile.setPinCode("100001");
            customerProfile.setNomineeName("Jane Doe");
            customerProfile.setNomineeRelation("Spouse");
            
            customerRepository.save(customerProfile);
            System.out.println("Seeded default Customer profile for John Doe");
        }
    }
}

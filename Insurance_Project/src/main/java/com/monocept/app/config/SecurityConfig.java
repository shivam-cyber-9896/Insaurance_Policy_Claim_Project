package com.monocept.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.monocept.app.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		return http.csrf(csrf -> csrf.disable())

				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/api/auth/**").permitAll()

						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**", "/api/plans/**").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")
						.requestMatchers("/api/products/**", "/api/plans/**").hasRole("ADMIN")

						.requestMatchers("/api/users/**").hasRole("ADMIN")

						.requestMatchers("/api/claims/{id}/review").hasRole("AGENT")

						.requestMatchers("/api/claims/{id}/decision").hasRole("ADMIN")

						// Customers Module
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/customers").hasRole("CUSTOMER")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/customers/**").hasRole("CUSTOMER")
						.requestMatchers("/api/customers/profile").hasRole("CUSTOMER")
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/customers").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")

						// Policies Module
						.requestMatchers("/api/policies/purchase").hasRole("CUSTOMER")
						.requestMatchers("/api/policies/issue").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers("/api/policies/my").hasRole("CUSTOMER")
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/policies").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers("/api/policies/{id}/cancel").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers("/api/policies/**").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")

						// Payments Module
						.requestMatchers("/api/payments/my").hasRole("CUSTOMER")
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/payments").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers("/api/payments/**").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")

						// Claims Module
						.requestMatchers("/api/claims/my").hasRole("CUSTOMER")
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/claims").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers("/api/claims/**").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")

						// Claim History Module
						.requestMatchers("/api/claim-history/**").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")

						.anyRequest().authenticated())
				
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

				.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {

		return config.getAuthenticationManager();
	}

}

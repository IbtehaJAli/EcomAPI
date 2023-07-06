package com.ibtehaj.Ecom;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
	Optional<CustomerProfile> findByEmail(String email);

	Optional<CustomerProfile> findByCustomerName(String customerName);
}
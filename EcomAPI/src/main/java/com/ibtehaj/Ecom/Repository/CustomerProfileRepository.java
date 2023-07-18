package com.ibtehaj.Ecom.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ibtehaj.Ecom.Models.CustomerProfile;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
	Optional<CustomerProfile> findByEmail(String email);

	Optional<CustomerProfile> findByCustomerName(String customerName);
}
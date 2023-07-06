package com.ibtehaj.Ecom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

	List<Sale> findByCustomer(CustomerProfile customer);
   
}
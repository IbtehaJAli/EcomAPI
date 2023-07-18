package com.ibtehaj.Ecom.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ibtehaj.Ecom.Models.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	Product findByCode(String code);

}

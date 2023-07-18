package com.ibtehaj.Ecom.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ibtehaj.Ecom.Models.AccessTokens;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessTokens, Long> {

	AccessTokens findByUsername(String username);
	AccessTokens findByAccesstoken(String access_token);
}

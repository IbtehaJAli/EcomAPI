package com.ibtehaj.Ecom;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessTokens, Long> {

	AccessTokens findByUsername(String username);
	AccessTokens findByAccesstoken(String access_token);
}

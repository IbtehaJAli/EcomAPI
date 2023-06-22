package com.ibtehaj.Ecom;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<BlacklistedToken, Long> {

    Optional<BlacklistedToken> findByTokenValue(String tokenValue);
}

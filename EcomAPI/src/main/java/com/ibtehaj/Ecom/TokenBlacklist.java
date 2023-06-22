package com.ibtehaj.Ecom;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklist {

    private final TokenBlacklistRepository repository;

    @Autowired
    public TokenBlacklist(TokenBlacklistRepository repository) {
        this.repository = repository;
    }

    public void add(String tokenValue) {
        repository.save(new BlacklistedToken(tokenValue));
    }

    public void remove(String tokenValue) {
        repository.findByTokenValue(tokenValue).ifPresent(repository::delete);
    }

    public boolean contains(String tokenValue) {
        return repository.findByTokenValue(tokenValue).isPresent();
    }
    
    public List<String> getAllTokenValues() {
        return repository.findAll().stream()
                .map(BlacklistedToken::getTokenValue)
                .collect(Collectors.toList());
    }
}

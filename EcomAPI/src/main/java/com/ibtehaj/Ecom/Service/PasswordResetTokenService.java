package com.ibtehaj.Ecom.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibtehaj.Ecom.Models.PasswordResetToken;
import com.ibtehaj.Ecom.Repository.PasswordResetTokenRepository;

@Service
public class PasswordResetTokenService {
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetToken save(PasswordResetToken token) {
        return passwordResetTokenRepository.save(token);
    }

    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    public void delete(PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }
}

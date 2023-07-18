package com.ibtehaj.Ecom.Utils;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ibtehaj.Ecom.Exception.CustomAccessDeniedException;
import com.ibtehaj.Ecom.Models.AccessTokens;
import com.ibtehaj.Ecom.Repository.AccessTokenRepository;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AccessTokenUtils {

    private final AccessTokenRepository accessTokenRepository;

    public AccessTokenUtils(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    public String getUsernameFromAccessToken() throws CustomAccessDeniedException {
    	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            AccessTokens access_token =accessTokenRepository.findByAccesstoken(accessToken);
            if(access_token==null) {
            	throw new CustomAccessDeniedException("Please login");
            }
            username=access_token.getUsername();
        }
        return username;
    }
}

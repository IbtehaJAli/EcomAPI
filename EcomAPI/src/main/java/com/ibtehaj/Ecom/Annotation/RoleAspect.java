package com.ibtehaj.Ecom.Annotation;

import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ibtehaj.Ecom.Exception.CustomAccessDeniedException;
import com.ibtehaj.Ecom.Models.AccessTokens;
import com.ibtehaj.Ecom.Models.User;
import com.ibtehaj.Ecom.Models.UserRole;
import com.ibtehaj.Ecom.Repository.AccessTokenRepository;
import com.ibtehaj.Ecom.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RoleAspect {
    private final AccessTokenRepository accessTokenRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public RoleAspect(AccessTokenRepository accessTokenRepository, UserRepository userRepository) {
        this.accessTokenRepository = accessTokenRepository;
        this.userRepository = userRepository;
    }

    @Before("@annotation(RestrictedToAdmin)")
    public void checkRole(JoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            AccessTokens access_token = accessTokenRepository.findByAccesstoken(accessToken);
            String username = access_token.getUsername();
            User user = userRepository.findByUsername(username);
            Set<UserRole> roles = user.getRoles();
            //System.out.println(roles);
            if (!roles.contains(UserRole.ROLE_ADMIN)) {
                throw new CustomAccessDeniedException("User does not have the required role");
            }
        }
    }
}

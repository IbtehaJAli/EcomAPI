package com.ibtehaj.Ecom;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class BlacklistAspect{

    private final TokenBlacklist blacklist;
    
    @Autowired
    public BlacklistAspect(TokenBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @Before("@annotation(CheckBlacklist)")
    public void checkBlacklist(JoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            if (blacklist.contains(accessToken)) {
            	throw new TokenRevokedException("The access token has been revoked. Please login.");
                
            }
        }
    }
}

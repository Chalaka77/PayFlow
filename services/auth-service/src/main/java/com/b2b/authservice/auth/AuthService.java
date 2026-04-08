package com.b2b.authservice.auth;

import com.b2b.authservice.security.JwtTokenService;
import org.springframework.stereotype.Service;

@Service
public class AuthService
{
    private final JwtTokenService jwtTokenService;

    public AuthService(JwtTokenService jwtTokenService)
    {
        this.jwtTokenService = jwtTokenService;
    }


    public String login(String username, String password)
    {
        if("admin".equals(username) && "password".equals(password))
        {
            return jwtTokenService.generateToken(username, "ADMIN");
        }

        if("user".equals(username) && "password".equals(password))
        {
            return jwtTokenService.generateToken(username, "USER");
        }
        throw new RuntimeException("Invalid credentials");
    }



}

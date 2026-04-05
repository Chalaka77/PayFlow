package com.b2b.paymentservice.auth;

import com.b2b.paymentservice.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService
{
    private final JwtUtil jwtUtil;

    public AuthService(JwtUtil jwtUtil)
    {
        this.jwtUtil = jwtUtil;
    }


    public String login(String username, String password)
    {
        if("admin".equals(username) && "admin".equals(password))
        {
            return jwtUtil.generateToken(username);
        }
        throw new RuntimeException("Invalid credentials");
    }



}

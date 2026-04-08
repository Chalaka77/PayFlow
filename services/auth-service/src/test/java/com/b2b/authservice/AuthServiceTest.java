package com.b2b.authservice;

import com.b2b.authservice.auth.AuthService;
import com.b2b.authservice.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest
{
    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_admin_success()
    {
        when(jwtTokenService.generateToken("admin", "ADMIN")).thenReturn("loginAdminToken");

        String token = authService.login("admin", "password");

        assertThat(token).isEqualTo("loginAdminToken");
        verify(jwtTokenService, times(1)).generateToken("admin", "ADMIN");
    }

    @Test
    void login_user_success()
    {
        when(jwtTokenService.generateToken("user", "USER")).thenReturn("loginUserToken");

        String token = authService.login("user", "password");

        assertThat(token).isEqualTo("loginUserToken");
        verify(jwtTokenService, times(1)).generateToken("user", "USER");
    }

    @Test
    void login_invalidCredentials_throwsException()
    {
        assertThatThrownBy(() -> authService.login("hacker", "wrongpassword"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");

        verify(jwtTokenService, never()).generateToken(any(), any());
    }

    @Test
    void login_correctUsername_wrongPassword_throwsException()
    {
        assertThatThrownBy(() -> authService.login("admin", "wrongpassword"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");

        verify(jwtTokenService, never()).generateToken(any(), any());
    }
}

package com.az.unitech.services;

import com.az.unitech.models.User;
import com.az.unitech.repositories.UserRepository;
import com.az.unitech.requests.AuthenticationRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsServiceImp userDetailsService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountService accountService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, userDetailsService, jwtTokenService, userRepository, passwordEncoder, accountService);

    }

    @Test
    void testLoginSuccess() {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("username", "password");
        UserDetails userDetails = mock(UserDetails.class);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(authenticationRequest.getPin())).thenReturn(userDetails);

        ResponseEntity<?> responseEntity = authService.login(authenticationRequest, response);

        assertEquals("User logged in!", responseEntity.getBody());
        verify(jwtTokenService).addTokenToResponse(userDetails, response);
    }


    @Test
    void testLoginFailure() {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("username", "password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Authentication failed"));
        ResponseEntity<?> responseEntity = authService.login(authenticationRequest, response);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Incorrect username or password", responseEntity.getBody());
    }

    @Test
    void testRegisterSuccess() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("username", "password");
        when(userDetailsService.loadUserByUsername(authenticationRequest.getPin())).thenReturn(null);
        when(passwordEncoder.encode(authenticationRequest.getPassword())).thenReturn("encodedPassword");
        User savedUser = mock(User.class);
        when(userRepository.save(any())).thenReturn(savedUser);

        ResponseEntity<?> responseEntity = authService.register(authenticationRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User registered!", responseEntity.getBody());
        verify(accountService).generateRandomAccounts(savedUser);
    }

    @Test
    void testRegisterUserAlreadyExists() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("existingUser", "password");
        when(userDetailsService.loadUserByUsername(authenticationRequest.getPin())).thenReturn(mock(UserDetails.class));

        ResponseEntity<?> responseEntity = authService.register(authenticationRequest);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("User already exists", responseEntity.getBody());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterFailure() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("username", "password");
        when(userDetailsService.loadUserByUsername(authenticationRequest.getPin())).thenReturn(null);
        when(passwordEncoder.encode(authenticationRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenThrow(new RuntimeException("Saving user failed"));

        ResponseEntity<?> responseEntity = authService.register(authenticationRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Error occurred during user registration", responseEntity.getBody());
    }
}

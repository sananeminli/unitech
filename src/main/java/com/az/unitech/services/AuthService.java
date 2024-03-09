package com.az.unitech.services;

import com.az.unitech.models.User;
import com.az.unitech.repositories.UserRepository;
import com.az.unitech.requests.AuthenticationRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service

@NoArgsConstructor
public class AuthService {
    @Autowired
    private  AuthenticationManager authenticationManager;
    @Autowired
    private  UserDetailsServiceImp userDetailsService;
    @Autowired
    private  JwtTokenService jwtTokenService;

    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountService accountService;



    public AuthService(AuthenticationManager authenticationManager, UserDetailsServiceImp userDetailsService, JwtTokenService jwtTokenService, UserRepository userRepository, PasswordEncoder passwordEncoder, AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }


    public ResponseEntity login(AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getPin(), authenticationRequest.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getPin());
            jwtTokenService.addTokenToResponse(userDetails, response);
            return ResponseEntity.ok("User logged in!");
        }
         catch (Exception e) {
            System.out.println("Error occurred during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Incorrect username or password");
        }
    }


    public ResponseEntity register(AuthenticationRequest authenticationRequest) {
        try {
            String pin = authenticationRequest.getPin();
            String password = authenticationRequest.getPassword();

            if (pin == null || pin.isEmpty() || password == null || password.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pin and password cannot be null or empty");
            }
           if (userDetailsService.loadUserByUsername(pin) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
            }

            User newUser = new User();
            newUser.setPin(pin);
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);

            User savedUser = userRepository.save(newUser);
            accountService.generateRandomAccounts(savedUser);

            return ResponseEntity.ok().body("User registered!");
        } catch (Exception e) {
            System.out.println("Error occurred during user registration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during user registration");
        }
    }

}

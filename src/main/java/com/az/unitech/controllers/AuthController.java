package com.az.unitech.controllers;

import com.az.unitech.repositories.UserRepository;
import com.az.unitech.services.AuthService;
import com.az.unitech.services.JwtTokenService;
import com.az.unitech.services.UserDetailsServiceImp;
import com.az.unitech.requests.AuthenticationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin


@Tag(name = "User login and register Apis.", description = "Endpoints for login and registering users.")
public class AuthController {



    @Autowired
    private AuthService authService;




    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest , HttpServletResponse response) {
        return authService.login(authenticationRequest , response);
    }

    @PostMapping("/register")
    public  ResponseEntity<?>  register(@RequestBody AuthenticationRequest authenticationRequest){
        return authService.register(authenticationRequest) ;
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(){
        return ResponseEntity.ok().build();
    }


}


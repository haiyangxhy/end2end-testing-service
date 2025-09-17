package com.testplatform.controller;

import com.testplatform.security.JwtRequest;
import com.testplatform.security.JwtResponse;
import com.testplatform.security.JwtTokenUtil;
import com.testplatform.security.InMemoryUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private InMemoryUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        System.out.println("Login request received for user: " + authenticationRequest.getUsername());
        
        try {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            throw e;
        }
        
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        
        // 添加密码验证调试信息
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("User details username: " + userDetails.getUsername());
        System.out.println("User details password: " + userDetails.getPassword());
        System.out.println("Provided password: " + authenticationRequest.getPassword());
        System.out.println("Password matches: " + encoder.matches(authenticationRequest.getPassword(), userDetails.getPassword()));
        
        final String token = jwtTokenUtil.generateToken(userDetails.getUsername());
        
        System.out.println("Token generated successfully for user: " + authenticationRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @GetMapping("/test-password")
    public ResponseEntity<?> testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String dbPassword = "$2a$10$JpQCqztIDwt7yhf.0kGQNutGnq/XW6olHOXybuRFwEc8Fjim4sjrO";
        String inputPassword = "password";
        String encodePasswordString = encoder.encode(inputPassword);
        boolean matches = encoder.matches(inputPassword, dbPassword);
        
        return ResponseEntity.ok("Password matches: " + matches + "\n" + "Encoded password: " + encodePasswordString);
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            System.out.println("Authenticating user: " + username);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            System.out.println("Authentication successful for user: " + username);
        } catch (DisabledException e) {
            System.out.println("User disabled: " + username);
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            System.out.println("Invalid credentials for user: " + username);
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
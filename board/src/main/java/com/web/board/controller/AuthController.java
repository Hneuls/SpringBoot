package com.web.board.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.web.board.config.JwtTokenProvider;
import com.web.board.entity.User;
import com.web.board.repository.UserRepository;
@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            User user = userRepository.findByLoginId(username)
            .orElseThrow(() -> new AuthenticationException("User not found") {});

            String nickname = user.getNickname();
            String token = jwtTokenProvider.createToken(username, nickname);

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("token", token);
            response.put("nickname", nickname);

            return ResponseEntity.ok(response);


        } catch(AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.status(401).body(error);
        }
    }
    
}

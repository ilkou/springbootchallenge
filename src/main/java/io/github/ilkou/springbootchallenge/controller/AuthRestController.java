package io.github.ilkou.springbootchallenge.controller;

import io.github.ilkou.springbootchallenge.security.jwt.JwtUtil;
import io.github.ilkou.springbootchallenge.service.UserService;
import io.github.ilkou.springbootchallenge.transverse.dto.AuthTokenDto;
import io.github.ilkou.springbootchallenge.transverse.dto.CtUserDto;
import io.github.ilkou.springbootchallenge.transverse.dto.LoginDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserService service;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<AuthTokenDto> login(@Valid @RequestBody LoginDto loginUser) {
        String token;
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginUser.getUsername(),
                            loginUser.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            CtUserDto userProfile = service.getUserProfile(authentication.getName());
            token = jwtUtil.generateToken(authentication, userProfile.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(new AuthTokenDto(token));
    }
}

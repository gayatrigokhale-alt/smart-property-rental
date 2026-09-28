package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import com.rental.smart_property_rental.dto.LoginRequest;
import com.rental.smart_property_rental.dto.RegisterRequest;
import com.rental.smart_property_rental.model.Tenant;
import com.rental.smart_property_rental.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        try {

            Tenant tenant = authService.registerTenant(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(tenant);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        try {

            Tenant tenant = authService.loginTenant(
                    request.getEmail(),
                    request.getPassword()
            );

            return ResponseEntity.ok(tenant);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    // Change password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String email,
            @Valid @RequestBody ChangePasswordRequest request) {

        try {
            Tenant tenant = authService.changePassword(
                    email,
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            return ResponseEntity.ok(tenant);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("Current password is incorrect")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
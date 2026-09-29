
        package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.dto.LandlordUpdateRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.rental.smart_property_rental.dto.LandlordChangePasswordRequest;
import com.rental.smart_property_rental.dto.LandlordLoginRequest;
import com.rental.smart_property_rental.dto.LandlordRegisterRequest;
import com.rental.smart_property_rental.model.Landlord;
import com.rental.smart_property_rental.service.LandlordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/landlords")
public class LandlordController {

    private final LandlordService landlordService;

    public LandlordController(LandlordService landlordService) {
        this.landlordService = landlordService;
    }

    @GetMapping
    public List<Landlord> getAllLandlords() {
        return landlordService.getAllLandlords();
    }

    // Landlord registration
    @PostMapping("/register")
    public ResponseEntity<?> registerLandlord(
            @Valid @RequestBody LandlordRegisterRequest request) {

        try {

            Landlord landlord =
                    landlordService.registerLandlord(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(landlord);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // Landlord login
    @PostMapping("/login")
    public ResponseEntity<?> loginLandlord(
            @Valid @RequestBody LandlordLoginRequest request) {

        try {

            Landlord landlord =
                    landlordService.loginLandlord(
                            request.getEmail(),
                            request.getPassword()
                    );

            return ResponseEntity.ok(landlord);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }
    //Changepassword
// Landlord change password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String email,
            @Valid @RequestBody LandlordChangePasswordRequest request) {

        try {

            Landlord landlord =
                    landlordService.changePassword(
                            email,
                            request.getCurrentPassword(),
                            request.getNewPassword()
                    );

            return ResponseEntity.ok(landlord);

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

    //Landlord profile update

// Landlord profile update
    @PutMapping("/{landlordId}")
    public ResponseEntity<?> updateLandlord(
            @PathVariable String landlordId,
            @Valid @RequestBody LandlordUpdateRequest request) {

        try {

            Landlord landlord =
                    landlordService.updateLandlord(
                            landlordId,
                            request
                    );

            return ResponseEntity.ok(landlord);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("Email already registered")) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }

            if (e.getMessage().equals("Landlord not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }



}


package com.rental.smart_property_rental.controller;

import jakarta.validation.Valid;
import com.rental.smart_property_rental.dto.RegisterRequest;
import com.rental.smart_property_rental.dto.LoginRequest;
import com.rental.smart_property_rental.dto.TenantLoginResponse;
import com.rental.smart_property_rental.dto.UpdateTenantRequest;
import com.rental.smart_property_rental.model.Tenant;
import com.rental.smart_property_rental.service.TenantService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    // =========================================================
    // TENANT LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<TenantLoginResponse> login(
            @RequestBody LoginRequest request) {

        Tenant tenant = tenantService.login(
                request.getEmail(),
                request.getPassword()
        );

        TenantLoginResponse response = new TenantLoginResponse(
                tenant.getTenantId(),
                tenant.getFirstName(),
                tenant.getLastName(),
                tenant.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GET ALL TENANTS
    // =========================================================

    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    // =========================================================
    // GET TENANT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(
            @PathVariable String id) {

        return tenantService.getTenantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // GET TENANT PROFILE BY EMAIL
    // =========================================================

    @GetMapping("/profile/{email}")
    public ResponseEntity<?> getProfile(
            @PathVariable String email) {

        try {

            Tenant tenant = tenantService.getTenantByEmail(email);

            return ResponseEntity.ok(tenant);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // =========================================================
    // UPDATE TENANT PROFILE
    // =========================================================

    @PutMapping("/profile/{email}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String email,
            @Valid @RequestBody UpdateTenantRequest updatedTenant) {

        try {

            Tenant tenant = tenantService.updateTenantProfile(
                    email,
                    updatedTenant
            );

            return ResponseEntity.ok(tenant);

        } catch (RuntimeException e) {

            if ("Email already registered".equals(e.getMessage())) {

                return ResponseEntity
                        .badRequest()
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // =========================================================
// CREATE TENANT ACCOUNT
// =========================================================

    @PostMapping
    public ResponseEntity<?> createTenant(
            @Valid @RequestBody RegisterRequest request) {

        try {

            Tenant tenant =
                    tenantService.registerTenant(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(tenant);

        } catch (RuntimeException e) {

            if ("Email already registered".equals(e.getMessage())) {

                return ResponseEntity
                        .badRequest()
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }



    // =========================================================
    // DELETE TENANT
    // =========================================================

    // DELETE TENANT ACCOUNT
    // =========================================================
// DELETE TENANT ACCOUNT
// =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTenant(
            @PathVariable String id,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        // =========================================================
        // CHECK LOGIN HEADER
        // =========================================================

        if (userId == null || userId.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"User ID is required\"}");
        }

        // =========================================================
        // TENANT CAN DELETE ONLY THEIR OWN ACCOUNT
        // =========================================================

        if (!id.equals(userId)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"You can only delete your own account\"}");
        }

        // =========================================================
        // DELETE ACCOUNT
        // =========================================================

        try {

            if (tenantService.getTenantById(id).isEmpty()) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            tenantService.deleteTenant(id);

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
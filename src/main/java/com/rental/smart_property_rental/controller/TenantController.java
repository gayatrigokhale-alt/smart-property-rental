package com.rental.smart_property_rental.controller;

import jakarta.validation.Valid;

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
    // CREATE TENANT
    // =========================================================

    @PostMapping
    public Tenant createTenant(
            @RequestBody Tenant tenant) {

        return tenantService.saveTenant(tenant);
    }

    // =========================================================
    // UPDATE TENANT BY ID
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(
            @PathVariable String id,
            @RequestBody Tenant tenant) {

        return tenantService.getTenantById(id)
                .map(existingTenant -> {

                    tenant.setId(id);

                    return ResponseEntity.ok(
                            tenantService.saveTenant(tenant)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // DELETE TENANT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable String id) {

        if (tenantService.getTenantById(id).isPresent()) {

            tenantService.deleteTenant(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
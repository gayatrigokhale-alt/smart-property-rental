package com.rental.smart_property_rental.controller;

import jakarta.validation.Valid;
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

    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable String id) {
        return tenantService.getTenantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<?> getProfile(@PathVariable String email) {
        try {
            Tenant tenant = tenantService.getTenantByEmail(email);
            return ResponseEntity.ok(tenant);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/profile/{email}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String email,
            @Valid  @RequestBody UpdateTenantRequest updatedTenant) {

        try {
            Tenant tenant = tenantService.updateTenantProfile(
                    email,
                    updatedTenant
            );

            return ResponseEntity.ok(tenant);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("Email already registered")) {
                return ResponseEntity
                        .badRequest()
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public Tenant createTenant(@RequestBody Tenant tenant) {
        return tenantService.saveTenant(tenant);
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable String id) {

        if (tenantService.getTenantById(id).isPresent()) {
            tenantService.deleteTenant(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
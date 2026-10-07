package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.model.TenantPreference;
import com.rental.smart_property_rental.service.TenantPreferenceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tenant-preferences")
public class TenantPreferenceController {

    private final TenantPreferenceService tenantPreferenceService;

    public TenantPreferenceController(
            TenantPreferenceService tenantPreferenceService) {

        this.tenantPreferenceService = tenantPreferenceService;
    }

    // =====================================================
    // GET
    // =====================================================

    @GetMapping("/{tenantId}")
    public ResponseEntity<?> getPreferences(
            @PathVariable String tenantId) {

        Optional<TenantPreference> preference =
                tenantPreferenceService
                        .getPreferencesByTenantId(tenantId);

        if (preference.isPresent()) {
            return ResponseEntity.ok(preference.get());
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        "Preferences not found for tenant: "
                                + tenantId
                );
    }

    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping
    public ResponseEntity<?> createPreferences(
            @Valid @RequestBody TenantPreference preference) {

        try {

            TenantPreference savedPreference =
                    tenantPreferenceService.createPreferences(
                            preference
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedPreference);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePreferences(
            @PathVariable String id,
            @Valid @RequestBody TenantPreference preference) {

        try {

            TenantPreference updatedPreference =
                    tenantPreferenceService.updatePreferences(
                            id,
                            preference
                    );

            return ResponseEntity.ok(updatedPreference);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (RuntimeException e) {

            if ("Preferences not found".equals(e.getMessage())) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePreferences(
            @PathVariable String id) {

        try {

            tenantPreferenceService.deletePreferences(id);

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.model.TenantAmenityPreference;
import com.rental.smart_property_rental.service.TenantAmenityPreferenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tenant-amenity-preferences")
public class TenantAmenityPreferenceController {

    private final TenantAmenityPreferenceService
            tenantAmenityPreferenceService;

    public TenantAmenityPreferenceController(
            TenantAmenityPreferenceService tenantAmenityPreferenceService) {

        this.tenantAmenityPreferenceService =
                tenantAmenityPreferenceService;
    }

    // GET
    @GetMapping("/{tenantId}")
    public ResponseEntity<?> getPreferences(
            @PathVariable String tenantId) {

        Optional<TenantAmenityPreference> preference =
                tenantAmenityPreferenceService
                        .getPreferencesByTenantId(tenantId);

        if (preference.isPresent()) {
            return ResponseEntity.ok(preference.get());
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        "Amenity preferences not found for tenant: "
                                + tenantId
                );
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> createPreferences(
            @RequestBody TenantAmenityPreference preference) {

        try {

            TenantAmenityPreference savedPreference =
                    tenantAmenityPreferenceService
                            .createPreferences(preference);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedPreference);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePreferences(
            @PathVariable String id,
            @RequestBody TenantAmenityPreference preference) {

        try {

            TenantAmenityPreference updatedPreference =
                    tenantAmenityPreferenceService
                            .updatePreferences(
                                    id,
                                    preference
                            );

            return ResponseEntity.ok(updatedPreference);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePreferences(
            @PathVariable String id) {

        try {

            tenantAmenityPreferenceService
                    .deletePreferences(id);

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
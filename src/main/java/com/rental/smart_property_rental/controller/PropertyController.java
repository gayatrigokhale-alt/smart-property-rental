package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.dto.PropertyResponse;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.service.PropertyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    // =====================================================
    // GET ALL PROPERTIES
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Property>> getAllProperties() {

        return ResponseEntity.ok(
                propertyService.getAllProperties()
        );
    }

    // =====================================================
    // GET ONLY AVAILABLE PROPERTIES
    // =====================================================

    @GetMapping("/available")
    public ResponseEntity<List<PropertyResponse>>
    getAvailableProperties() {

        return ResponseEntity.ok(
                propertyService.getAvailableProperties()
        );
    }

    // =====================================================
    // GET PROPERTY BY PROPERTY_ID
    // =====================================================

    @GetMapping("/{propertyId}")
    public ResponseEntity<?> getProperty(
            @PathVariable String propertyId) {

        return propertyService
                .getPropertyByPropertyId(propertyId)
                .<ResponseEntity<?>>map(
                        ResponseEntity::ok
                )
                .orElseGet(() ->
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Property not found")
                );
    }

    // =====================================================
    // GET PROPERTIES OF A PARTICULAR LANDLORD
    // =====================================================

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<?> getLandlordProperties(
            @PathVariable String landlordId,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        if (userId == null || userId.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        if (!userId.equals(landlordId)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to view these properties"
                    );
        }

        return ResponseEntity.ok(
                propertyService.getPropertiesByLandlord(
                        landlordId
                )
        );
    }

    // =====================================================
    // CREATE PROPERTY
    // =====================================================

    @PostMapping
    public ResponseEntity<?> createProperty(
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId,

            @Valid @RequestBody Property property) {

        try {

            Property savedProperty =
                    propertyService.createProperty(
                            property,
                            userId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedProperty);

        } catch (RuntimeException e) {

            if ("X-User-Id header is required"
                    .equals(e.getMessage())) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // UPDATE PROPERTY
    // =====================================================

    @PutMapping("/{propertyId}")
    public ResponseEntity<?> updateProperty(
            @PathVariable String propertyId,

            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId,

            @Valid @RequestBody Property property) {

        if (userId == null || userId.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {

            Property updatedProperty =
                    propertyService.updateProperty(
                            propertyId,
                            property,
                            userId
                    );

            return ResponseEntity.ok(updatedProperty);

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            if ("Property not found"
                    .equals(e.getMessage())) {

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
    // DELETE PROPERTY
    // =====================================================

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<?> deleteProperty(
            @PathVariable String propertyId,

            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        if (userId == null || userId.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {

            propertyService.deleteProperty(
                    propertyId,
                    userId
            );

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            if (e.getMessage().contains("active lease")) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }

            if ("Property not found"
                    .equals(e.getMessage())) {

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
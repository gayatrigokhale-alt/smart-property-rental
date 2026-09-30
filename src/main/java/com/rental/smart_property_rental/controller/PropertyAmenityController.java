package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.model.PropertyAmenity;
import com.rental.smart_property_rental.service.PropertyAmenityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/property-amenities")
public class PropertyAmenityController {

    private final PropertyAmenityService propertyAmenityService;

    public PropertyAmenityController(
            PropertyAmenityService propertyAmenityService) {

        this.propertyAmenityService = propertyAmenityService;
    }

    @GetMapping
    public ResponseEntity<List<PropertyAmenity>> getAllPropertyAmenities() {

        return ResponseEntity.ok(
                propertyAmenityService.getAllPropertyAmenities()
        );
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyAmenity> getPropertyAmenities(
            @PathVariable String propertyId) {

        return ResponseEntity.ok(
                propertyAmenityService.getPropertyAmenities(propertyId)
        );
    }

    @PostMapping
    public ResponseEntity<PropertyAmenity> createPropertyAmenities(
            @RequestHeader(value = "X-User-Id", required = false)
            String userId,
            @RequestBody PropertyAmenity propertyAmenity) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                propertyAmenityService.createPropertyAmenities(
                        propertyAmenity,
                        userId
                )
        );
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<PropertyAmenity> updatePropertyAmenities(
            @PathVariable String propertyId,
            @RequestHeader(value = "X-User-Id", required = false)
            String userId,
            @RequestBody PropertyAmenity propertyAmenity) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                propertyAmenityService.updatePropertyAmenities(
                        propertyId,
                        propertyAmenity,
                        userId
                )
        );
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> deletePropertyAmenities(
            @PathVariable String propertyId,
            @RequestHeader(value = "X-User-Id", required = false)
            String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        propertyAmenityService.deletePropertyAmenities(
                propertyId,
                userId
        );

        return ResponseEntity.noContent().build();
    }
}

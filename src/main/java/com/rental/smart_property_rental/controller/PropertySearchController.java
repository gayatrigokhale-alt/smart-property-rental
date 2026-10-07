package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.dto.PropertyResponse;
import com.rental.smart_property_rental.service.PropertySearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertySearchController {

    private final PropertySearchService propertySearchService;

    public PropertySearchController(
            PropertySearchService propertySearchService) {

        this.propertySearchService = propertySearchService;
    }

    @GetMapping("/search/{tenantId}")
    public ResponseEntity<?> searchProperties(
            @PathVariable String tenantId,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        // Authentication check
        if (userId == null || userId.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"User ID is required\"}");
        }

        // Authorization check
        if (!tenantId.equals(userId)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"You can only search properties using your own tenant ID\"}");
        }

        List<PropertyResponse> properties =
                propertySearchService.searchProperties(tenantId);

        return ResponseEntity.ok(properties);
    }
}
package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.model.Lease;
import com.rental.smart_property_rental.service.LeaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leases")
public class LeaseController {

    private final LeaseService leaseService;

    public LeaseController(LeaseService leaseService) {
        this.leaseService = leaseService;
    }

    // GET ALL LEASES
    @GetMapping
    public ResponseEntity<List<Lease>> getAllLeases() {
        return ResponseEntity.ok(leaseService.getAllLeases());
    }

    // GET LEASE BY ID
    @GetMapping("/{leaseId}")
    public ResponseEntity<?> getLeaseById(
            @PathVariable String leaseId) {

        try {
            return ResponseEntity.ok(
                    leaseService.getLeaseById(leaseId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // GET LEASES BY TENANT
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<?> getLeasesByTenant(
            @PathVariable String tenantId) {

        return ResponseEntity.ok(
                leaseService.getLeasesByTenant(tenantId)
        );
    }

    // GET LEASES BY PROPERTY
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<?> getLeasesByProperty(
            @PathVariable String propertyId) {

        return ResponseEntity.ok(
                leaseService.getLeasesByProperty(propertyId)
        );
    }

    // CREATE LEASE
    @PostMapping
    public ResponseEntity<?> createLease(
            @RequestHeader(value = "X-User-Id", required = false)
            String userId,
            @RequestBody Lease lease) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(leaseService.createLease(lease, userId));

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // UPDATE LEASE
    @PutMapping("/{leaseId}")
    public ResponseEntity<?> updateLease(
            @PathVariable String leaseId,
            @RequestHeader(value = "X-User-Id", required = false)
            String userId,
            @RequestBody Lease lease) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {
            return ResponseEntity.ok(
                    leaseService.updateLease(
                            leaseId,
                            lease,
                            userId
                    )
            );

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // TERMINATE LEASE
    @PutMapping("/{leaseId}/terminate")
    public ResponseEntity<?> terminateLease(
            @PathVariable String leaseId,
            @RequestHeader(value = "X-User-Id", required = false)
            String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {
            return ResponseEntity.ok(
                    leaseService.terminateLease(
                            leaseId,
                            userId
                    )
            );

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // COMPLETE LEASE
    @PutMapping("/{leaseId}/complete")
    public ResponseEntity<?> completeLease(
            @PathVariable String leaseId,
            @RequestHeader(value = "X-User-Id", required = false)
            String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {
            return ResponseEntity.ok(
                    leaseService.completeLease(
                            leaseId,
                            userId
                    )
            );

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
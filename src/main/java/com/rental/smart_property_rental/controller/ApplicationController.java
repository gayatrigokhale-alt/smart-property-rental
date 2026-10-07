package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.model.Application;
import com.rental.smart_property_rental.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(
                applicationService.getAllApplications()
        );
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<?> getApplicationById(
            @PathVariable String applicationId) {

        try {
            return ResponseEntity.ok(
                    applicationService.getApplicationById(applicationId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<?> getApplicationsByTenant(
            @PathVariable String tenantId,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User ID is required");
        }

        if (!userId.equals(tenantId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to view these applications"
                    );
        }

        try {
            return ResponseEntity.ok(
                    applicationService.getApplicationsByTenant(tenantId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<?> getApplicationsByProperty(
            @PathVariable String propertyId,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User ID is required");
        }

        try {

            List<Application> applications =
                    applicationService.getApplicationsByProperty(
                            propertyId,
                            userId
                    );

            return ResponseEntity.ok(applications);

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")
                    || e.getMessage().contains("not verified")) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            if (e.getMessage().contains("not found")) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createApplication(
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId,
            @RequestBody Application application) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User ID is required");
        }

        try {
            Application createdApplication =
                    applicationService.createApplication(
                            application,
                            userId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdApplication);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable String applicationId,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId,
            @RequestBody Application application) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User ID is required");
        }

        try {
            Application updatedApplication =
                    applicationService.updateApplicationStatus(
                            applicationId,
                            application.getApplicationStatus(),
                            userId
                    );

            return ResponseEntity.ok(updatedApplication);

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{applicationId}/withdraw")
    public ResponseEntity<?> withdrawApplication(
            @PathVariable String applicationId,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User ID is required");
        }

        try {
            Application withdrawnApplication =
                    applicationService.withdrawApplication(
                            applicationId,
                            userId
                    );

            return ResponseEntity.ok(withdrawnApplication);

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<?> deleteApplication(
            @PathVariable String applicationId,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User ID is required");
        }

        try {
            applicationService.deleteApplication(
                    applicationId,
                    userId
            );

            return ResponseEntity.ok(
                    "Application deleted successfully"
            );

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not authorized")) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
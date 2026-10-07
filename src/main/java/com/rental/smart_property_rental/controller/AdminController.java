package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.dto.AdminLoginRequest;
import com.rental.smart_property_rental.dto.AdminLoginResponse;
import com.rental.smart_property_rental.model.Admin;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.model.Tenant;
import com.rental.smart_property_rental.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    // =====================================================
    // ADMIN LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(
            @RequestBody AdminLoginRequest request) {

        Admin admin =
                adminService.login(
                        request.getEmail(),
                        request.getPassword()
                );

        AdminLoginResponse response =
                new AdminLoginResponse(
                        admin.getAdminId(),
                        admin.getFirstName(),
                        admin.getLastName(),
                        admin.getEmail()
                );

        return ResponseEntity.ok(response);
    }


    // =====================================================
    // SYSTEM STATISTICS
    // =====================================================

    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics(
            @RequestHeader(
                    value = "X-Admin-Id",
                    required = false
            ) String adminId) {

        if (!adminService.isValidAdmin(adminId)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing Admin ID");
        }

        return ResponseEntity.ok(
                adminService.getStatistics()
        );
    }


    // =====================================================
    // VIEW ALL TENANTS
    // =====================================================

    @GetMapping("/tenants")
    public ResponseEntity<?> getAllTenants(
            @RequestHeader(
                    value = "X-Admin-Id",
                    required = false
            ) String adminId) {

        if (!adminService.isValidAdmin(adminId)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing Admin ID");
        }

        return ResponseEntity.ok(
                adminService.getAllTenants()
        );
    }


    // =====================================================
    // VIEW ALL LANDLORDS
    // =====================================================

    @GetMapping("/landlords")
    public ResponseEntity<?> getAllLandlords(
            @RequestHeader(
                    value = "X-Admin-Id",
                    required = false
            ) String adminId) {

        if (!adminService.isValidAdmin(adminId)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing Admin ID");
        }

        return ResponseEntity.ok(
                adminService.getAllLandlords()
        );
    }


    // =====================================================
    // VIEW ALL PROPERTIES
    // =====================================================

    @GetMapping("/properties")
    public ResponseEntity<?> getAllProperties(
            @RequestHeader(
                    value = "X-Admin-Id",
                    required = false
            ) String adminId) {

        if (!adminService.isValidAdmin(adminId)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing Admin ID");
        }

        List<Property> properties =
                adminService.getAllProperties();

        return ResponseEntity.ok(properties);
    }


    // =====================================================
    // VERIFY / REJECT LANDLORD
    // =====================================================

    @PutMapping("/landlords/{landlordId}/verification")
    public ResponseEntity<?> updateLandlordVerification(
            @PathVariable String landlordId,

            @RequestHeader(
                    value = "X-Admin-Id",
                    required = false
            ) String adminId,

            @RequestBody Map<String, String> request) {

        if (!adminService.isValidAdmin(adminId)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing Admin ID");
        }

        String status =
                request.get("status");

        if (status == null || status.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Verification status is required");
        }

        try {

            return ResponseEntity.ok(
                    adminService.updateLandlordVerification(
                            landlordId,
                            status
                    )
            );

        } catch (RuntimeException e) {

            if ("Landlord not found"
                    .equals(e.getMessage())) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.dto.AdminLoginRequest;
import com.rental.smart_property_rental.dto.AdminLoginResponse;
import com.rental.smart_property_rental.model.Admin;
import com.rental.smart_property_rental.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(
            @RequestBody AdminLoginRequest request) {

        Admin admin = adminService.login(
                request.getEmail(),
                request.getPassword()
        );

        AdminLoginResponse response = new AdminLoginResponse(
                admin.getAdminId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
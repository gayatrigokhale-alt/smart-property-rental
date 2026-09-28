package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.dto.RegisterRequest;
import com.rental.smart_property_rental.model.Tenant;
import com.rental.smart_property_rental.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final PasswordService passwordService;

    public AuthService(TenantRepository tenantRepository,
                       PasswordService passwordService) {
        this.tenantRepository = tenantRepository;
        this.passwordService = passwordService;
    }

    public Tenant registerTenant(RegisterRequest request) {

        // Check whether email already exists
        if (tenantRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create new Tenant
        Tenant tenant = new Tenant();

        tenant.setFirstName(request.getFirstName());
        tenant.setLastName(request.getLastName());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setGender(request.getGender());
        tenant.setOccupation(request.getOccupation());
        tenant.setAge(request.getAge());
        tenant.setTenantType(request.getTenantType());
        tenant.setHasPets(request.getHasPets());

        // Hash the password
        String hashedPassword =
                passwordService.hashPassword(request.getPassword());

        tenant.setPasswordHash(hashedPassword);

        // Generate a unique Tenant ID
        int nextNumber = (int) tenantRepository.count() + 1;
        String tenantId = "T" + String.format("%03d", nextNumber);

        while (tenantRepository.findByTenantId(tenantId).isPresent()) {
            nextNumber++;
            tenantId = "T" + String.format("%03d", nextNumber);
        }

        tenant.setTenantId(tenantId);

        // Save tenant
        return tenantRepository.save(tenant);
    }

    public Tenant loginTenant(String email, String password) {

        Tenant tenant = tenantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        boolean passwordCorrect =
                passwordService.verifyPassword(
                        password,
                        tenant.getPasswordHash()
                );

        if (!passwordCorrect) {
            throw new RuntimeException("Invalid email or password");
        }

        return tenant;
    }

    //Change password
    public Tenant changePassword(
            String email,
            String currentPassword,
            String newPassword) {

        Tenant tenant = tenantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Tenant not found"));

        boolean passwordCorrect =
                passwordService.verifyPassword(
                        currentPassword,
                        tenant.getPasswordHash()
                );

        if (!passwordCorrect) {
            throw new RuntimeException("Current password is incorrect");
        }

        String newHashedPassword =
                passwordService.hashPassword(newPassword);

        tenant.setPasswordHash(newHashedPassword);

        return tenantRepository.save(tenant);
    }
}
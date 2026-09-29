package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.exception.InvalidCredentialsException;
import com.rental.smart_property_rental.dto.UpdateTenantRequest;
import com.rental.smart_property_rental.model.Tenant;
import com.rental.smart_property_rental.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PasswordService passwordService;

    public TenantService(TenantRepository tenantRepository,
                         PasswordService passwordService) {

        this.tenantRepository = tenantRepository;
        this.passwordService = passwordService;
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Optional<Tenant> getTenantById(String id) {
        return tenantRepository.findById(id);
    }

    public Tenant getTenantByEmail(String email) {

        return tenantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Tenant not found"));
    }

    public Tenant updateTenantProfile(
            String email,
            UpdateTenantRequest updatedTenant) {

        Tenant existingTenant = tenantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Tenant not found"));

        // Check whether the new email is already used
        // by another tenant
        if (!email.equals(updatedTenant.getEmail())) {

            Optional<Tenant> tenantWithNewEmail =
                    tenantRepository.findByEmail(updatedTenant.getEmail());

            if (tenantWithNewEmail.isPresent()) {
                throw new RuntimeException("Email already registered");
            }

            existingTenant.setEmail(updatedTenant.getEmail());
        }

        // Update profile information
        existingTenant.setFirstName(updatedTenant.getFirstName());
        existingTenant.setLastName(updatedTenant.getLastName());
        existingTenant.setPhone(updatedTenant.getPhone());
        existingTenant.setGender(updatedTenant.getGender());
        existingTenant.setOccupation(updatedTenant.getOccupation());
        existingTenant.setAge(updatedTenant.getAge());
        existingTenant.setTenantType(updatedTenant.getTenantType());
        existingTenant.setHasPets(updatedTenant.getHasPets());

        // Tenant_ID is NOT changed.
        // Password_Hash is NOT changed here.

        return tenantRepository.save(existingTenant);
    }

    public Tenant saveTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public void deleteTenant(String id) {
        tenantRepository.deleteById(id);
    }

    // Tenant login
    public Tenant login(String email, String password) {

        Tenant tenant = tenantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        boolean passwordCorrect = passwordService.verifyPassword(
                password,
                tenant.getPasswordHash()
        );

        if (!passwordCorrect) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return tenant;
    }
}
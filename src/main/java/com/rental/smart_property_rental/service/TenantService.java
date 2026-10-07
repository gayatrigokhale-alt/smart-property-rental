package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.dto.RegisterRequest;
import com.rental.smart_property_rental.dto.UpdateTenantRequest;
import com.rental.smart_property_rental.exception.InvalidCredentialsException;
import com.rental.smart_property_rental.model.Tenant;
import com.rental.smart_property_rental.repository.ApplicationRepository;
import com.rental.smart_property_rental.repository.LeaseRepository;
import com.rental.smart_property_rental.repository.TenantAmenityPreferenceRepository;
import com.rental.smart_property_rental.repository.TenantPreferenceRepository;
import com.rental.smart_property_rental.repository.TenantRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PasswordService passwordService;
    private final ApplicationRepository applicationRepository;
    private final LeaseRepository leaseRepository;
    private final TenantPreferenceRepository tenantPreferenceRepository;
    private final TenantAmenityPreferenceRepository tenantAmenityPreferenceRepository;

    public TenantService(
            TenantRepository tenantRepository,
            PasswordService passwordService,
            ApplicationRepository applicationRepository,
            LeaseRepository leaseRepository,
            TenantPreferenceRepository tenantPreferenceRepository,
            TenantAmenityPreferenceRepository tenantAmenityPreferenceRepository) {

        this.tenantRepository = tenantRepository;
        this.passwordService = passwordService;
        this.applicationRepository = applicationRepository;
        this.leaseRepository = leaseRepository;
        this.tenantPreferenceRepository = tenantPreferenceRepository;
        this.tenantAmenityPreferenceRepository =
                tenantAmenityPreferenceRepository;
    }

    // =========================================================
    // GET ALL TENANTS
    // =========================================================

    public List<Tenant> getAllTenants() {

        return tenantRepository.findAll();
    }

    // =========================================================
    // GET TENANT BY ID
    // =========================================================

    public Optional<Tenant> getTenantById(String id) {

        return tenantRepository.findByTenantId(id);
    }

    // =========================================================
    // GET TENANT BY EMAIL
    // =========================================================

    public Tenant getTenantByEmail(String email) {

        return tenantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Tenant not found"));
    }

    // =========================================================
    // CREATE TENANT ACCOUNT
    // =========================================================

    public Tenant registerTenant(RegisterRequest request) {

        // Check duplicate email
        if (tenantRepository
                .findByEmail(request.getEmail())
                .isPresent()) {

            throw new RuntimeException(
                    "Email already registered"
            );
        }

        // Create new tenant
        Tenant tenant = new Tenant();

        // Generate Tenant_ID automatically
        String tenantId = generateTenantId();

        tenant.setTenantId(tenantId);

        // Set profile information
        tenant.setFirstName(request.getFirstName());
        tenant.setLastName(request.getLastName());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setGender(request.getGender());
        tenant.setOccupation(request.getOccupation());
        tenant.setAge(request.getAge());
        tenant.setTenantType(request.getTenantType());
        tenant.setHasPets(request.getHasPets());

        // Hash password before storing
        tenant.setPasswordHash(
                passwordService.hashPassword(
                        request.getPassword()
                )
        );

        return tenantRepository.save(tenant);
    }

    // =========================================================
    // GENERATE TENANT ID
    // =========================================================

    private String generateTenantId() {

        long count = tenantRepository.count() + 1;

        String tenantId = String.format(
                "T%03d",
                count
        );

        // Make sure the generated ID does not already exist
        while (tenantRepository
                .findByTenantId(tenantId)
                .isPresent()) {

            count++;

            tenantId = String.format(
                    "T%03d",
                    count
            );
        }

        return tenantId;
    }

    // =========================================================
    // UPDATE TENANT PROFILE
    // =========================================================

    public Tenant updateTenantProfile(
            String email,
            UpdateTenantRequest updatedTenant) {

        Tenant existingTenant =
                tenantRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant not found"
                                ));

        // Check whether the new email is already used
        // by another tenant
        if (!email.equals(updatedTenant.getEmail())) {

            Optional<Tenant> tenantWithNewEmail =
                    tenantRepository.findByEmail(
                            updatedTenant.getEmail()
                    );

            if (tenantWithNewEmail.isPresent()) {

                throw new RuntimeException(
                        "Email already registered"
                );
            }

            existingTenant.setEmail(
                    updatedTenant.getEmail()
            );
        }

        // Update profile information
        existingTenant.setFirstName(
                updatedTenant.getFirstName()
        );

        existingTenant.setLastName(
                updatedTenant.getLastName()
        );

        existingTenant.setPhone(
                updatedTenant.getPhone()
        );

        existingTenant.setGender(
                updatedTenant.getGender()
        );

        existingTenant.setOccupation(
                updatedTenant.getOccupation()
        );

        existingTenant.setAge(
                updatedTenant.getAge()
        );

        existingTenant.setTenantType(
                updatedTenant.getTenantType()
        );

        existingTenant.setHasPets(
                updatedTenant.getHasPets()
        );

        // Tenant_ID is NOT changed.
        // Password_Hash is NOT changed here.

        return tenantRepository.save(existingTenant);
    }

    // =========================================================
    // DELETE TENANT ACCOUNT
    // =========================================================

    public void deleteTenant(String id) {

        // -----------------------------------------------------
        // STEP 1: CHECK THAT TENANT EXISTS
        // -----------------------------------------------------

        Tenant tenant =
                tenantRepository
                        .findByTenantId(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant not found"
                                ));

        // -----------------------------------------------------
        // STEP 2: CHECK FOR ACTIVE LEASE
        // -----------------------------------------------------

        boolean hasActiveLease =
                leaseRepository
                        .findByTenantId(id)
                        .stream()
                        .anyMatch(lease ->
                                "Active".equalsIgnoreCase(
                                        lease.getLeaseStatus()
                                )
                        );

        if (hasActiveLease) {

            throw new RuntimeException(
                    "You cannot delete your account while you have an active lease."
            );
        }

        // -----------------------------------------------------
        // STEP 3: CHECK FOR PENDING APPLICATION
        // -----------------------------------------------------

        boolean hasPendingApplication =
                applicationRepository
                        .existsByTenantIdAndApplicationStatus(
                                id,
                                "Pending"
                        );

        if (hasPendingApplication) {

            throw new RuntimeException(
                    "You have a pending application. Withdraw the application before deleting your account."
            );
        }

        // -----------------------------------------------------
        // STEP 4: DELETE ALL RENTAL PREFERENCES
        // -----------------------------------------------------

        tenantPreferenceRepository
                .deleteByTenantId(id);

        // -----------------------------------------------------
        // STEP 5: DELETE ALL AMENITY PREFERENCES
        // -----------------------------------------------------

        tenantAmenityPreferenceRepository
                .deleteByTenantId(id);

        // -----------------------------------------------------
        // STEP 6: DELETE TENANT ACCOUNT
        // -----------------------------------------------------

        tenantRepository.delete(tenant);
    }

    // =========================================================
    // TENANT LOGIN
    // =========================================================

    public Tenant login(
            String email,
            String password) {

        Tenant tenant =
                tenantRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid email or password"
                                ));

        boolean passwordCorrect =
                passwordService.verifyPassword(
                        password,
                        tenant.getPasswordHash()
                );

        if (!passwordCorrect) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        return tenant;
    }
}
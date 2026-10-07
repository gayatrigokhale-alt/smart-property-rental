package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.exception.InvalidCredentialsException;
import com.rental.smart_property_rental.model.Admin;
import com.rental.smart_property_rental.model.Landlord;
import com.rental.smart_property_rental.model.Tenant;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.repository.AdminRepository;
import com.rental.smart_property_rental.repository.LandlordRepository;
import com.rental.smart_property_rental.repository.TenantRepository;
import com.rental.smart_property_rental.repository.PropertyRepository;
import com.rental.smart_property_rental.repository.ApplicationRepository;
import com.rental.smart_property_rental.repository.LeaseRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final LandlordRepository landlordRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final ApplicationRepository applicationRepository;
    private final LeaseRepository leaseRepository;
    private final PasswordService passwordService;

    public AdminService(
            AdminRepository adminRepository,
            LandlordRepository landlordRepository,
            TenantRepository tenantRepository,
            PropertyRepository propertyRepository,
            ApplicationRepository applicationRepository,
            LeaseRepository leaseRepository,
            PasswordService passwordService) {

        this.adminRepository = adminRepository;
        this.landlordRepository = landlordRepository;
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
        this.applicationRepository = applicationRepository;
        this.leaseRepository = leaseRepository;
        this.passwordService = passwordService;
    }


    // =====================================================
    // ADMIN LOGIN
    // =====================================================

    public Admin login(String email, String password) {

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        ));

        boolean passwordCorrect =
                passwordService.verifyPassword(
                        password,
                        admin.getPasswordHash()
                );

        if (!passwordCorrect) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        return admin;
    }


    // =====================================================
    // VERIFY ADMIN
    // =====================================================

    public boolean isValidAdmin(String adminId) {

        if (adminId == null || adminId.isBlank()) {
            return false;
        }

        return adminRepository
                .findByAdminId(adminId)
                .isPresent();
    }


    // =====================================================
    // SYSTEM STATISTICS
    // =====================================================

    public Map<String, Long> getStatistics() {

        long totalTenants =
                tenantRepository.count();

        long totalLandlords =
                landlordRepository.count();

        long totalProperties =
                propertyRepository.count();

        long totalApplications =
                applicationRepository.count();

        long totalLeases =
                leaseRepository.count();

        long pendingLandlords =
                landlordRepository.findAll()
                        .stream()
                        .filter(landlord ->
                                "Pending".equalsIgnoreCase(
                                        landlord.getVerificationStatus()
                                ))
                        .count();

        Map<String, Long> statistics =
                new HashMap<>();

        statistics.put(
                "totalTenants",
                totalTenants
        );

        statistics.put(
                "totalLandlords",
                totalLandlords
        );

        statistics.put(
                "totalProperties",
                totalProperties
        );

        statistics.put(
                "totalApplications",
                totalApplications
        );

        statistics.put(
                "totalLeases",
                totalLeases
        );

        statistics.put(
                "pendingLandlords",
                pendingLandlords
        );

        return statistics;
    }


    // =====================================================
    // VIEW ALL TENANTS
    // =====================================================

    public List<Tenant> getAllTenants() {

        return tenantRepository.findAll();
    }


    // =====================================================
    // VIEW ALL PROPERTIES
    // =====================================================

    public List<Property> getAllProperties() {

        return propertyRepository.findAll();
    }


    // =====================================================
    // VIEW ALL LANDLORDS
    // =====================================================

    public List<Map<String, Object>> getAllLandlords() {

        return landlordRepository.findAll()
                .stream()
                .map(this::landlordToMap)
                .toList();
    }


    // =====================================================
    // LANDLORD VERIFICATION
    // =====================================================

    public Map<String, Object> updateLandlordVerification(
            String landlordId,
            String status) {

        Landlord landlord =
                landlordRepository.findByLandlordId(landlordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Landlord not found"
                                ));

        if (!status.equals("Verified")
                && !status.equals("Rejected")
                && !status.equals("Pending")) {

            throw new RuntimeException(
                    "Invalid verification status"
            );
        }

        landlord.setVerificationStatus(status);

        Landlord savedLandlord =
                landlordRepository.save(landlord);

        return landlordToMap(savedLandlord);
    }


    // =====================================================
    // LANDLORD RESPONSE
    // =====================================================

    private Map<String, Object> landlordToMap(
            Landlord landlord) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "landlordId",
                landlord.getLandlordId()
        );

        data.put(
                "firstName",
                landlord.getFirstName()
        );

        data.put(
                "lastName",
                landlord.getLastName()
        );

        data.put(
                "email",
                landlord.getEmail()
        );

        data.put(
                "phone",
                landlord.getPhone()
        );

        data.put(
                "verificationStatus",
                landlord.getVerificationStatus()
        );

        return data;
    }
}
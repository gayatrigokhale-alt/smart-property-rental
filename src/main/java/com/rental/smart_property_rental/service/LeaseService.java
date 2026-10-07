package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.model.Application;
import com.rental.smart_property_rental.model.Lease;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.model.TenantPreference;

import com.rental.smart_property_rental.repository.ApplicationRepository;
import com.rental.smart_property_rental.repository.LandlordRepository;
import com.rental.smart_property_rental.repository.LeaseRepository;
import com.rental.smart_property_rental.repository.PropertyRepository;
import com.rental.smart_property_rental.repository.TenantPreferenceRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final ApplicationRepository applicationRepository;
    private final PropertyRepository propertyRepository;
    private final LandlordRepository landlordRepository;
    private final TenantPreferenceRepository tenantPreferenceRepository;

    public LeaseService(
            LeaseRepository leaseRepository,
            ApplicationRepository applicationRepository,
            PropertyRepository propertyRepository,
            LandlordRepository landlordRepository,
            TenantPreferenceRepository tenantPreferenceRepository) {

        this.leaseRepository = leaseRepository;
        this.applicationRepository = applicationRepository;
        this.propertyRepository = propertyRepository;
        this.landlordRepository = landlordRepository;
        this.tenantPreferenceRepository = tenantPreferenceRepository;
    }

    /*
     * CREATE LEASE AUTOMATICALLY
     * This method is called only when a landlord approves an application.
     *
     * The lease receives:
     * - Start_Date from the application's proposed move-in date.
     * - End_Date from the start date + minimum lease duration.
     *
     * IMPORTANT:
     * The End_Date is only stored as part of the lease.
     * It does NOT automatically expire the lease.
     */
    public Lease createLeaseFromApprovedApplication(
            String applicationId,
            String userId) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("User ID is required");
        }

        // Landlord must be verified.
        requireVerifiedLandlord(userId);

        Application application =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Application not found"
                                ));

        /*
         * A lease can only be generated from
         * an approved application.
         */
        if (!"Approved".equalsIgnoreCase(
                application.getApplicationStatus())) {

            throw new RuntimeException(
                    "Lease can only be created for an approved application"
            );
        }

        Property property =
                propertyRepository
                        .findByPropertyId(
                                application.getPropertyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        /*
         * Only the landlord who owns the property
         * can generate the lease.
         */
        if (!property.getLandlordId().equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to create this lease"
            );
        }

        /*
         * Prevent duplicate leases for the same application.
         */
        if (leaseRepository
                .findByApplicationId(
                        application.getApplicationId()
                )
                .isPresent()) {

            throw new RuntimeException(
                    "A lease already exists for this application"
            );
        }

        /*
         * Get the tenant's lease preference.
         */
        TenantPreference preference =
                tenantPreferenceRepository
                        .findByTenantId(
                                application.getTenantId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant preferences not found"
                                ));

        /*
         * The application's proposed move-in date
         * becomes the lease start date.
         */
        String startDate =
                application.getProposedMoveInDate();

        if (startDate == null || startDate.isBlank()) {

            throw new RuntimeException(
                    "Lease start date is missing"
            );
        }

        /*
         * Lease duration is taken from
         * TENANT_PREFERENCE.
         */
        Integer leaseDuration =
                preference.getMinimumLeaseDuration();

        if (leaseDuration == null ||
                leaseDuration <= 0) {

            throw new RuntimeException(
                    "Valid lease duration is required"
            );
        }

        /*
         * Calculate the lease end date.
         *
         * This calculates and stores the contractual
         * end date only.
         *
         * There is NO automatic expiry logic.
         */
        LocalDate start =
                LocalDate.parse(startDate);

        LocalDate end =
                start.plusMonths(leaseDuration);

        Lease lease = new Lease();

        String leaseId =
                generateLeaseId();

        lease.setLeaseId(leaseId);

        lease.setApplicationId(
                application.getApplicationId()
        );

        lease.setTenantId(
                application.getTenantId()
        );

        lease.setPropertyId(
                application.getPropertyId()
        );

        // Lease dates.
        lease.setStartDate(
                start.toString()
        );

        lease.setEndDate(
                end.toString()
        );

        /*
         * Rent and security deposit are taken
         * directly from the property.
         */
        lease.setMonthlyRent(
                property.getMonthlyRent()
        );

        lease.setSecurityDeposit(
                property.getSecurityDeposit()
        );

        lease.setLeaseStatus(
                "Active"
        );

        return leaseRepository.save(lease);
    }

    // ============================================================
    // GET ALL LEASES
    // ============================================================

    public List<Lease> getAllLeases() {

        return leaseRepository.findAll();
    }

    // ============================================================
    // GET LEASE BY ID
    // ============================================================

    public Lease getLeaseById(
            String leaseId) {

        return leaseRepository
                .findByLeaseId(leaseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lease not found"
                        ));
    }

    // ============================================================
    // GET LEASES BY TENANT
    // ============================================================

    public List<Lease> getLeasesByTenant(
            String tenantId) {

        return leaseRepository
                .findByTenantId(tenantId);
    }

    // ============================================================
    // GET LEASES BY PROPERTY
    // ============================================================

    public List<Lease> getLeasesByProperty(
            String propertyId,
            String userId) {

        Property property =
                propertyRepository
                        .findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        // Only property owner can view these leases.
        if (!property.getLandlordId().equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to view leases for this property"
            );
        }

        // Landlord must be verified.
        requireVerifiedLandlord(userId);

        return leaseRepository
                .findByPropertyId(propertyId);
    }

    // ============================================================
    // UPDATE LEASE
    // ============================================================

    public Lease updateLease(
            String leaseId,
            Lease updatedLease,
            String userId) {

        Lease existingLease =
                leaseRepository
                        .findByLeaseId(leaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Lease not found"
                                ));

        Property property =
                propertyRepository
                        .findByPropertyId(
                                existingLease.getPropertyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        // Only property owner can update lease.
        if (!property.getLandlordId().equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to update this lease"
            );
        }

        // Landlord must be verified.
        requireVerifiedLandlord(userId);

        if (!"Active".equals(
                existingLease.getLeaseStatus())) {

            throw new RuntimeException(
                    "Only an active lease can be updated"
            );
        }

        /*
         * Only financial terms are updated here.
         * Lease dates remain unchanged.
         */
        existingLease.setMonthlyRent(
                updatedLease.getMonthlyRent()
        );

        existingLease.setSecurityDeposit(
                updatedLease.getSecurityDeposit()
        );

        return leaseRepository.save(
                existingLease
        );
    }

    // ============================================================
    // TERMINATE LEASE
    // ============================================================

    public Lease terminateLease(
            String leaseId,
            String userId) {

        Lease existingLease =
                leaseRepository
                        .findByLeaseId(leaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Lease not found"
                                ));

        Property property =
                propertyRepository
                        .findByPropertyId(
                                existingLease.getPropertyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        if (!property.getLandlordId().equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to terminate this lease"
            );
        }

        // Landlord must be verified.
        requireVerifiedLandlord(userId);

        if (!"Active".equals(
                existingLease.getLeaseStatus())) {

            throw new RuntimeException(
                    "Only an active lease can be terminated"
            );
        }

        existingLease.setLeaseStatus(
                "Terminated"
        );

        /*
         * Record the date on which the landlord
         * manually terminated the lease.
         *
         * This is NOT automatic expiry.
         */
        existingLease.setTerminationDate(
                LocalDate.now().toString()
        );

        // Property becomes available again.
        property.setPropertyStatus(
                "Available"
        );

        propertyRepository.save(property);

        return leaseRepository.save(
                existingLease
        );
    }

    // ============================================================
    // COMPLETE LEASE
    // ============================================================

    public Lease completeLease(
            String leaseId,
            String userId) {

        Lease existingLease =
                leaseRepository
                        .findByLeaseId(leaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Lease not found"
                                ));

        Property property =
                propertyRepository
                        .findByPropertyId(
                                existingLease.getPropertyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        if (!property.getLandlordId().equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to complete this lease"
            );
        }

        // Landlord must be verified.
        requireVerifiedLandlord(userId);

        if (!"Active".equals(
                existingLease.getLeaseStatus())) {

            throw new RuntimeException(
                    "Only an active lease can be completed"
            );
        }

        existingLease.setLeaseStatus(
                "Completed"
        );

        /*
         * Do NOT check End_Date here.
         *
         * Completion happens only because the landlord
         * explicitly requested it.
         */

        // Property becomes available again.
        property.setPropertyStatus(
                "Available"
        );

        propertyRepository.save(property);

        return leaseRepository.save(
                existingLease
        );
    }

    // ============================================================
    // CHECK LANDLORD VERIFICATION
    // ============================================================

    private boolean isLandlordVerified(
            String landlordId) {

        if (landlordId == null ||
                landlordId.isBlank()) {

            return false;
        }

        return landlordRepository
                .findByLandlordId(landlordId)
                .map(landlord ->
                        "Verified".equalsIgnoreCase(
                                landlord.getVerificationStatus()
                        ))
                .orElse(false);
    }

    private void requireVerifiedLandlord(
            String landlordId) {

        if (!isLandlordVerified(landlordId)) {

            throw new RuntimeException(
                    "Landlord account is not verified"
            );
        }
    }

    // ============================================================
    // GENERATE LEASE ID
    // ============================================================

    private String generateLeaseId() {

        long count =
                leaseRepository.count() + 1;

        String leaseId =
                String.format(
                        "LEASE%03d",
                        count
                );

        while (leaseRepository
                .existsByLeaseId(leaseId)) {

            count++;

            leaseId =
                    String.format(
                            "LEASE%03d",
                            count
                    );
        }

        return leaseId;
    }
}
package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.model.Application;
import com.rental.smart_property_rental.model.Landlord;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.model.TenantPreference;

import com.rental.smart_property_rental.repository.ApplicationRepository;
import com.rental.smart_property_rental.repository.LandlordRepository;
import com.rental.smart_property_rental.repository.PropertyRepository;
import com.rental.smart_property_rental.repository.TenantPreferenceRepository;
import com.rental.smart_property_rental.repository.TenantRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TenantRepository tenantRepository;
    private final TenantPreferenceRepository tenantPreferenceRepository;
    private final PropertyRepository propertyRepository;
    private final LandlordRepository landlordRepository;
    private final LeaseService leaseService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            TenantRepository tenantRepository,
            TenantPreferenceRepository tenantPreferenceRepository,
            PropertyRepository propertyRepository,
            LandlordRepository landlordRepository,
            LeaseService leaseService) {

        this.applicationRepository = applicationRepository;
        this.tenantRepository = tenantRepository;
        this.tenantPreferenceRepository = tenantPreferenceRepository;
        this.propertyRepository = propertyRepository;
        this.landlordRepository = landlordRepository;
        this.leaseService = leaseService;
    }

    // ============================================================
    // GET ALL APPLICATIONS
    // ============================================================

    public List<Application> getAllApplications() {

        return applicationRepository.findAll();
    }

    // ============================================================
    // GET APPLICATION BY ID
    // ============================================================

    public Application getApplicationById(
            String applicationId) {

        return applicationRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Application not found"
                        ));
    }

    // ============================================================
    // GET APPLICATIONS BY TENANT
    // ============================================================

    public List<Application> getApplicationsByTenant(
            String tenantId) {

        if (!tenantRepository.existsByTenantId(tenantId)) {

            throw new RuntimeException(
                    "Tenant not found"
            );
        }

        return applicationRepository.findByTenantId(
                tenantId
        );
    }

    // ============================================================
    // GET APPLICATIONS BY PROPERTY
    // ============================================================

    public List<Application> getApplicationsByProperty(
            String propertyId,
            String userId) {

        Property property =
                propertyRepository
                        .findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        // Only the landlord who owns the property
        // can view its applications.
        if (!property.getLandlordId().equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to view applications for this property"
            );
        }

        // Landlord must be verified.
        requireVerifiedLandlord(userId);

        return applicationRepository
                .findByPropertyId(propertyId);
    }

    // ============================================================
    // CREATE APPLICATION
    // ============================================================

    public Application createApplication(
            Application application,
            String userId) {

        if (userId == null || userId.isBlank()) {

            throw new RuntimeException(
                    "User ID is required"
            );
        }

        // Tenant can only create an application
        // for their own account.
        if (!userId.equals(application.getTenantId())) {

            throw new RuntimeException(
                    "You can only create an application for your own tenant account"
            );
        }

        // Check that tenant exists.
        if (!tenantRepository.existsByTenantId(
                application.getTenantId())) {

            throw new RuntimeException(
                    "Tenant not found"
            );
        }

        // --------------------------------------------------------
        // GET TENANT PREFERENCES
        // --------------------------------------------------------

        TenantPreference preference =
                tenantPreferenceRepository
                        .findByTenantId(
                                application.getTenantId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant preferences not found"
                                ));

        // --------------------------------------------------------
        // GET PROPERTY
        // --------------------------------------------------------

        Property property =
                propertyRepository
                        .findByPropertyId(
                                application.getPropertyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));
        // --------------------------------------------------------
       // PROPERTY AVAILABILITY CHECK
        // --------------------------------------------------------

        if (!"Available".equalsIgnoreCase(
                property.getPropertyStatus())) {

            throw new RuntimeException(
                    "This property is no longer available"
            );
        }
        // --------------------------------------------------------
        // LANDLORD VERIFICATION
        // --------------------------------------------------------

        // --------------------------------------------------------
        // LANDLORD VERIFICATION
        // --------------------------------------------------------

        /*
         * Tenant can only apply to a property
         * owned by a verified landlord.
         *
         * Rejected and Pending landlords cannot
         * receive applications.
         */

        Landlord landlord =
                landlordRepository
                        .findByLandlordId(
                                property.getLandlordId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Landlord not found"
                                ));

        if ("Rejected".equalsIgnoreCase(
                landlord.getVerificationStatus())) {

            throw new RuntimeException(
                    "You cannot apply to a property owned by a rejected landlord"
            );
        }

        if (!"Verified".equalsIgnoreCase(
                landlord.getVerificationStatus())) {

            throw new RuntimeException(
                    "You cannot apply to a property owned by an unverified landlord"
            );
        }

        // --------------------------------------------------------
        // DUPLICATE APPLICATION CHECK
        // --------------------------------------------------------

        if (applicationRepository
                .existsByTenantIdAndPropertyId(
                        application.getTenantId(),
                        application.getPropertyId())) {

            throw new RuntimeException(
                    "You have already applied for this property"
            );
        }

        // --------------------------------------------------------
        // GENERATE APPLICATION ID
        // --------------------------------------------------------

        String applicationId =
                generateApplicationId();

        application.setApplicationId(
                applicationId
        );

        // --------------------------------------------------------
        // APPLICATION DATE
        // --------------------------------------------------------

        application.setApplicationDate(
                LocalDate.now().toString()
        );

        // --------------------------------------------------------
        // COPY TENANT PREFERENCES
        // --------------------------------------------------------

        /*
         * The tenant does not enter these values again
         * when applying.
         *
         * They are taken directly from TENANT_PREFERENCE.
         */

        application.setProposedMoveInDate(
                preference.getPreferredMoveInDate()
        );

        application.setNumberOfOccupants(
                preference.getNumberOfOccupants()
        );

        // --------------------------------------------------------
        // INITIAL APPLICATION STATUS
        // --------------------------------------------------------

        application.setApplicationStatus(
                "Pending"
        );

        // --------------------------------------------------------
        // SAVE APPLICATION
        // --------------------------------------------------------

        return applicationRepository.save(
                application
        );
    }

    // ============================================================
    // UPDATE APPLICATION STATUS
    // ============================================================

    public Application updateApplicationStatus(
            String applicationId,
            String status,
            String userId) {

        Application existingApplication =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Application not found"
                                ));

        Property property =
                propertyRepository
                        .findByPropertyId(
                                existingApplication.getPropertyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        // Only the landlord who owns the property
        // can update the application.
        if (!property.getLandlordId().equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to update this application"
            );
        }

        // Landlord must be verified.
        requireVerifiedLandlord(userId);

        // Validate status.
        if (!status.equals("Pending")
                && !status.equals("Approved")
                && !status.equals("Rejected")
                && !status.equals("Withdrawn")) {

            throw new RuntimeException(
                    "Invalid application status"
            );
        }

        /*
         * Only a pending application can be processed.
         *
         * This prevents an already approved application
         * from creating another lease.
         */
        if (!"Pending".equalsIgnoreCase(
                existingApplication.getApplicationStatus())) {

            throw new RuntimeException(
                    "Only a pending application can be approved or rejected"
            );
        }

        // ========================================================
        // APPROVE APPLICATION
        // ========================================================

        if ("Approved".equalsIgnoreCase(status)) {

            existingApplication.setApplicationStatus(
                    "Approved"
            );

            applicationRepository.save(
                    existingApplication
            );

            try {

                // Create lease automatically.
                leaseService.createLeaseFromApprovedApplication(
                        applicationId,
                        userId
                );

                /*
                 * The lease was successfully created.
                 * Now mark the property as Rented.
                 */
                property.setPropertyStatus("Rented");

                propertyRepository.save(property);

            } catch (RuntimeException e) {

                /*
                 * If lease creation fails,
                 * revert the application to Pending.
                 */
                existingApplication.setApplicationStatus(
                        "Pending"
                );

                applicationRepository.save(
                        existingApplication
                );

                throw e;
            }

            return existingApplication;
        }

        // ========================================================
        // REJECT / WITHDRAW / PENDING
        // ========================================================

        existingApplication.setApplicationStatus(
                status
        );

        return applicationRepository.save(
                existingApplication
        );
    }

    // ============================================================
    // WITHDRAW APPLICATION
    // ============================================================

    public Application withdrawApplication(
            String applicationId,
            String userId) {

        Application existingApplication =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Application not found"
                                ));

        // Only the tenant who created the application
        // can withdraw it.
        if (!existingApplication
                .getTenantId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to withdraw this application"
            );
        }

        // Only pending applications can be withdrawn.
        if (!"Pending".equalsIgnoreCase(
                existingApplication.getApplicationStatus())) {

            throw new RuntimeException(
                    "Only a pending application can be withdrawn"
            );
        }

        existingApplication.setApplicationStatus(
                "Withdrawn"
        );

        return applicationRepository.save(
                existingApplication
        );
    }

    // ============================================================
    // DELETE APPLICATION
    // ============================================================

    public void deleteApplication(
            String applicationId,
            String userId) {

        Application existingApplication =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Application not found"
                                ));

        // Tenant can delete their own application.
        if (existingApplication
                .getTenantId()
                .equals(userId)) {

            applicationRepository.delete(
                    existingApplication
            );

            return;
        }

        // Landlord can delete applications
        // belonging to their property.
        Property property =
                propertyRepository
                        .findByPropertyId(
                                existingApplication.getPropertyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        if (property.getLandlordId().equals(userId)) {

            // Landlord must be verified.
            requireVerifiedLandlord(userId);

            applicationRepository.delete(
                    existingApplication
            );

            return;
        }

        throw new RuntimeException(
                "You are not authorized to delete this application"
        );
    }

    // ============================================================
    // CHECK LANDLORD VERIFICATION
    // ============================================================

    private boolean isLandlordVerified(
            String landlordId) {

        if (landlordId == null
                || landlordId.isBlank()) {

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

    // ============================================================
    // REQUIRE VERIFIED LANDLORD
    // ============================================================

    private void requireVerifiedLandlord(
            String landlordId) {

        if (!isLandlordVerified(landlordId)) {

            throw new RuntimeException(
                    "Landlord account is not verified"
            );
        }
    }

    // ============================================================
    // GENERATE APPLICATION ID
    // ============================================================

    private String generateApplicationId() {

        long count =
                applicationRepository.count() + 1;

        String applicationId =
                String.format(
                        "APP%03d",
                        count
                );

        while (applicationRepository
                .existsByApplicationId(
                        applicationId)) {

            count++;

            applicationId =
                    String.format(
                            "APP%03d",
                            count
                    );
        }

        return applicationId;
    }
}
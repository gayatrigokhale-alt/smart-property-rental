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

    // GET ALL APPLICATIONS

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    // GET APPLICATION BY ID

    public Application getApplicationById(String applicationId) {
        return applicationRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));
    }

    // GET APPLICATIONS BY TENANT

    public List<Application> getApplicationsByTenant(String tenantId) {

        if (!tenantRepository.existsByTenantId(tenantId)) {
            throw new RuntimeException("Tenant not found");
        }

        return applicationRepository.findByTenantId(tenantId);
    }

    // GET APPLICATIONS BY PROPERTY

    public List<Application> getApplicationsByProperty(
            String propertyId,
            String userId) {

        Property property = propertyRepository
                .findByPropertyId(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to view applications for this property");
        }

        requireVerifiedLandlord(userId);

        return applicationRepository.findByPropertyId(propertyId);
    }

    // CREATE APPLICATION

    public Application createApplication(
            Application application,
            String userId) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("User ID is required");
        }

        if (!userId.equals(application.getTenantId())) {
            throw new RuntimeException(
                    "You can only create an application for your own tenant account");
        }

        if (!tenantRepository.existsByTenantId(
                application.getTenantId())) {
            throw new RuntimeException("Tenant not found");
        }

        // Get tenant preferences

        TenantPreference preference = tenantPreferenceRepository
                .findByTenantId(application.getTenantId())
                .orElseThrow(() ->
                        new RuntimeException("Tenant preferences not found"));

        // Get property

        Property property = propertyRepository
                .findByPropertyId(application.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        // Check property availability

        if (!"Available".equalsIgnoreCase(
                property.getPropertyStatus())) {
            throw new RuntimeException(
                    "This property is no longer available");
        }

        // Verify landlord

        Landlord landlord = landlordRepository
                .findByLandlordId(property.getLandlordId())
                .orElseThrow(() ->
                        new RuntimeException("Landlord not found"));

        if ("Rejected".equalsIgnoreCase(
                landlord.getVerificationStatus())) {
            throw new RuntimeException(
                    "You cannot apply to a property owned by a rejected landlord");
        }

        if (!"Verified".equalsIgnoreCase(
                landlord.getVerificationStatus())) {
            throw new RuntimeException(
                    "You cannot apply to a property owned by an unverified landlord");
        }

        // Prevent duplicate applications

        if (applicationRepository.existsByTenantIdAndPropertyId(
                application.getTenantId(),
                application.getPropertyId())) {
            throw new RuntimeException(
                    "You have already applied for this property");
        }

        // Set application ID and date

        application.setApplicationId(generateApplicationId());
        application.setApplicationDate(LocalDate.now().toString());

        // Validate tenant's preferred move-in date
        // Past dates are allowed, but the date must be valid
        // and cannot be before the property's availability date.

        String preferredMoveInDate =
                preference.getPreferredMoveInDate();

        String availableFrom = property.getAvailableFrom();

        if (preferredMoveInDate == null
                || preferredMoveInDate.isBlank()) {
            throw new RuntimeException(
                    "Preferred move-in date is required");
        }

        if (availableFrom == null || availableFrom.isBlank()) {
            throw new RuntimeException(
                    "Property availability date is missing");
        }

        LocalDate moveInDate;
        LocalDate availabilityDate;

        try {
            moveInDate = LocalDate.parse(preferredMoveInDate);
            availabilityDate = LocalDate.parse(availableFrom);
        } catch (java.time.format.DateTimeParseException exception) {
            throw new RuntimeException(
                    "Move-in date and property availability date must use YYYY-MM-DD format");
        }

        // Retained validation: Respect property availability date

        if (moveInDate.isBefore(availabilityDate)) {
            throw new RuntimeException(
                    "Preferred move-in date cannot be before the property's available date: "
                            + availableFrom);
        }

        // Copy tenant preferences into application

        application.setProposedMoveInDate(preferredMoveInDate);
        application.setNumberOfOccupants(
                preference.getNumberOfOccupants());

        // Set initial status

        application.setApplicationStatus("Pending");

        // Save application

        return applicationRepository.save(application);
    }

    // UPDATE APPLICATION STATUS

    public Application updateApplicationStatus(
            String applicationId,
            String status,
            String userId) {

        Application existingApplication = applicationRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        Property property = propertyRepository
                .findByPropertyId(existingApplication.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to update this application");
        }

        requireVerifiedLandlord(userId);

        if (status == null
                || (!status.equals("Pending")
                && !status.equals("Approved")
                && !status.equals("Rejected")
                && !status.equals("Withdrawn"))) {
            throw new RuntimeException("Invalid application status");
        }

        if (!"Pending".equalsIgnoreCase(
                existingApplication.getApplicationStatus())) {
            throw new RuntimeException(
                    "Only a pending application can be approved or rejected");
        }

        if ("Approved".equalsIgnoreCase(status)) {

            existingApplication.setApplicationStatus("Approved");
            property.setPropertyStatus("Reserved");

            Application savedApplication =
                    applicationRepository.save(existingApplication);

            propertyRepository.save(property);

            // Lease creation happens only after successful payment.

            return savedApplication;
        }

        existingApplication.setApplicationStatus(status);

        return applicationRepository.save(existingApplication);
    }

    // WITHDRAW APPLICATION

    public Application withdrawApplication(
            String applicationId,
            String userId) {

        Application existingApplication = applicationRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        if (!existingApplication.getTenantId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to withdraw this application");
        }

        if (!"Pending".equalsIgnoreCase(
                existingApplication.getApplicationStatus())) {
            throw new RuntimeException(
                    "Only a pending application can be withdrawn");
        }

        existingApplication.setApplicationStatus("Withdrawn");

        return applicationRepository.save(existingApplication);
    }

    // DELETE APPLICATION

    public void deleteApplication(
            String applicationId,
            String userId) {

        Application existingApplication = applicationRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        // Tenant can delete their own application.

        if (existingApplication.getTenantId().equals(userId)) {
            applicationRepository.delete(existingApplication);
            return;
        }

        // Landlord can delete applications for their property.

        Property property = propertyRepository
                .findByPropertyId(existingApplication.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (property.getLandlordId().equals(userId)) {
            requireVerifiedLandlord(userId);
            applicationRepository.delete(existingApplication);
            return;
        }

        throw new RuntimeException(
                "You are not authorized to delete this application");
    }

    // CHECK LANDLORD VERIFICATION

    private boolean isLandlordVerified(String landlordId) {

        if (landlordId == null || landlordId.isBlank()) {
            return false;
        }

        return landlordRepository
                .findByLandlordId(landlordId)
                .map(landlord ->
                        "Verified".equalsIgnoreCase(
                                landlord.getVerificationStatus()))
                .orElse(false);
    }

    // REQUIRE VERIFIED LANDLORD

    private void requireVerifiedLandlord(String landlordId) {

        if (!isLandlordVerified(landlordId)) {
            throw new RuntimeException(
                    "Landlord account is not verified");
        }
    }

    // GENERATE APPLICATION ID

    private String generateApplicationId() {

        long count = applicationRepository.count() + 1;

        String applicationId = String.format("APP%03d", count);

        while (applicationRepository.existsByApplicationId(
                applicationId)) {
            count++;
            applicationId = String.format("APP%03d", count);
        }

        return applicationId;
    }
}
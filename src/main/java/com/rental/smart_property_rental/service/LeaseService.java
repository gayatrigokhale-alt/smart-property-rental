
package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.repository.PaymentRepository;
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

    private final PaymentRepository paymentRepository;
    private final LeaseRepository leaseRepository;
    private final ApplicationRepository applicationRepository;
    private final PropertyRepository propertyRepository;
    private final LandlordRepository landlordRepository;
    private final TenantPreferenceRepository tenantPreferenceRepository;

    public LeaseService(
            PaymentRepository paymentRepository,
            LeaseRepository leaseRepository,
            ApplicationRepository applicationRepository,
            PropertyRepository propertyRepository,
            LandlordRepository landlordRepository,
            TenantPreferenceRepository tenantPreferenceRepository) {

        this.paymentRepository = paymentRepository;
        this.leaseRepository = leaseRepository;
        this.applicationRepository = applicationRepository;
        this.propertyRepository = propertyRepository;
        this.landlordRepository = landlordRepository;
        this.tenantPreferenceRepository = tenantPreferenceRepository;
    }



    // GET ALL LEASES
    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }

    // GET LEASE BY ID
    public Lease getLeaseById(String leaseId) {
        return leaseRepository.findByLeaseId(leaseId)
                .orElseThrow(() ->
                        new RuntimeException("Lease not found"));
    }

    // GET LEASES BY TENANT
    public List<Lease> getLeasesByTenant(String tenantId) {
        return leaseRepository.findByTenantId(tenantId);
    }

    // GET LEASES BY PROPERTY
    public List<Lease> getLeasesByProperty(
            String propertyId,
            String userId) {

        Property property = propertyRepository
                .findByPropertyId(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to view leases for this property");
        }

        requireVerifiedLandlord(userId);

        return leaseRepository.findByPropertyId(propertyId);
    }

    // UPDATE LEASE
    public Lease updateLease(
            String leaseId,
            Lease updatedLease,
            String userId) {

        Lease existingLease = leaseRepository.findByLeaseId(leaseId)
                .orElseThrow(() ->
                        new RuntimeException("Lease not found"));

        Property property = propertyRepository
                .findByPropertyId(existingLease.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to update this lease");
        }

        requireVerifiedLandlord(userId);

        if (!"Active".equals(existingLease.getLeaseStatus())) {
            throw new RuntimeException(
                    "Only an active lease can be updated");
        }

        existingLease.setMonthlyRent(updatedLease.getMonthlyRent());
        existingLease.setSecurityDeposit(
                updatedLease.getSecurityDeposit());

        return leaseRepository.save(existingLease);
    }

    // TERMINATE LEASE
    public Lease terminateLease(String leaseId, String userId) {

        Lease existingLease = leaseRepository.findByLeaseId(leaseId)
                .orElseThrow(() ->
                        new RuntimeException("Lease not found"));

        Property property = propertyRepository
                .findByPropertyId(existingLease.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to terminate this lease");
        }

        requireVerifiedLandlord(userId);

        if (!"Active".equals(existingLease.getLeaseStatus())) {
            throw new RuntimeException(
                    "Only an active lease can be terminated");
        }

        existingLease.setLeaseStatus("Terminated");
        existingLease.setTerminationDate(LocalDate.now().toString());

        property.setPropertyStatus("Available");
        propertyRepository.save(property);

        return leaseRepository.save(existingLease);
    }

    // COMPLETE LEASE
    public Lease completeLease(String leaseId, String userId) {

        Lease existingLease = leaseRepository.findByLeaseId(leaseId)
                .orElseThrow(() ->
                        new RuntimeException("Lease not found"));

        Property property = propertyRepository
                .findByPropertyId(existingLease.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to complete this lease");
        }

        requireVerifiedLandlord(userId);

        if (!"Active".equals(existingLease.getLeaseStatus())) {
            throw new RuntimeException(
                    "Only an active lease can be completed");
        }

        existingLease.setLeaseStatus("Completed");
        property.setPropertyStatus("Available");
        propertyRepository.save(property);

        return leaseRepository.save(existingLease);
    }

    // CHECK LANDLORD VERIFICATION
    private boolean isLandlordVerified(String landlordId) {

        if (landlordId == null || landlordId.isBlank()) {
            return false;
        }

        return landlordRepository.findByLandlordId(landlordId)
                .map(landlord ->
                        "Verified".equalsIgnoreCase(
                                landlord.getVerificationStatus()))
                .orElse(false);
    }

    private void requireVerifiedLandlord(String landlordId) {
        if (!isLandlordVerified(landlordId)) {
            throw new RuntimeException(
                    "Landlord account is not verified");
        }
    }

    // GENERATE LEASE ID
    private String generateLeaseId() {

        long count = leaseRepository.count() + 1;
        String leaseId = String.format("LEASE%03d", count);

        while (leaseRepository.existsByLeaseId(leaseId)) {
            count++;
            leaseId = String.format("LEASE%03d", count);
        }

        return leaseId;
    }

    // VALIDATE MOVE-IN DATE AGAINST PROPERTY AVAILABILITY
    private void validateMoveInDate(
            String startDate,
            Property property) {

        String availableFrom = property.getAvailableFrom();

        if (availableFrom == null || availableFrom.isBlank()) {
            throw new RuntimeException(
                    "Property availability date is missing");
        }

        LocalDate moveInDate = LocalDate.parse(startDate);
        LocalDate availabilityDate = LocalDate.parse(availableFrom);

        if (moveInDate.isBefore(availabilityDate)) {
            throw new RuntimeException(
                    "Move-in date cannot be before the property's available date: "
                            + availableFrom);
        }
    }

    public void validateLeaseCanBeCreated(
            String applicationId,
            String tenantId) {

        // 1. Find the application
        Application application = applicationRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        // 2. Verify tenant ownership
        if (tenantId == null || tenantId.isBlank()
                || !tenantId.equals(application.getTenantId())) {
            throw new RuntimeException(
                    "You are not authorized to create a lease for this application");
        }

        // 3. Application must be approved
        if (!"Approved".equalsIgnoreCase(
                application.getApplicationStatus())) {
            throw new RuntimeException(
                    "A lease can be created only for an approved application");
        }

        // 4. Prevent duplicate leases
        if (leaseRepository.findByApplicationId(applicationId).isPresent()) {
            throw new RuntimeException(
                    "A lease already exists for this application");
        }

        // 5. Find the property
        Property property = propertyRepository
                .findByPropertyId(application.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        // 6. Property must be reserved
        if (!"Reserved".equalsIgnoreCase(property.getPropertyStatus())) {
            throw new RuntimeException(
                    "Property is not reserved for this application");
        }

        // 7. Validate the proposed move-in date
        String moveInDate = application.getProposedMoveInDate();

        if (moveInDate == null || moveInDate.isBlank()) {
            throw new RuntimeException(
                    "Proposed move-in date is required");
        }

        validateMoveInDate(moveInDate, property);

        // 8. Validate the tenant's lease duration preference
        TenantPreference preference = tenantPreferenceRepository
                .findByTenantId(tenantId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tenant preference not found"));

        if (preference.getMinimumLeaseDuration() <= 0) {
            throw new RuntimeException(
                    "Minimum lease duration must be greater than zero");
        }

        // 9. Validate the property's rent and security deposit
        if (property.getMonthlyRent() == null
                || property.getSecurityDeposit() == null
                || property.getMonthlyRent() < 0
                || property.getSecurityDeposit() < 0) {
            throw new RuntimeException(
                    "Property rent or security deposit is invalid");
        }
    }

    // CREATE LEASE AFTER SUCCESSFUL PAYMENT
    public Lease createLeaseAfterSuccessfulPayment(
            String applicationId,
            String tenantId) {

        Application application = applicationRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        if (tenantId == null || tenantId.isBlank()
                || !tenantId.equals(application.getTenantId())) {
            throw new RuntimeException(
                    "You are not authorized to create this lease");
        }

        if (!"Approved".equalsIgnoreCase(
                application.getApplicationStatus())) {
            throw new RuntimeException("Application must be approved");
        }

        var existingLease = leaseRepository
                .findByApplicationId(applicationId);

        if (existingLease.isPresent()) {
            return existingLease.get();
        }

        paymentRepository.findByApplicationIdAndPaymentStatus(
                        applicationId, "Successful")
                .orElseThrow(() ->
                        new RuntimeException(
                                "Successful payment is required"));

        Property property = propertyRepository
                .findByPropertyId(application.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!"Reserved".equalsIgnoreCase(
                property.getPropertyStatus())) {
            throw new RuntimeException(
                    "Property is not reserved for this application");
        }

        TenantPreference preference = tenantPreferenceRepository
                .findByTenantId(application.getTenantId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tenant preferences not found"));

        String startDate = application.getProposedMoveInDate();

        if (startDate == null || startDate.isBlank()) {
            throw new RuntimeException("Lease start date is missing");
        }

        // NEW: Validate move-in date against property availability.
        validateMoveInDate(startDate, property);

        Integer duration = preference.getMinimumLeaseDuration();

        if (duration == null || duration <= 0) {
            throw new RuntimeException(
                    "Valid lease duration is required");
        }

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = start.plusMonths(duration);

        Lease lease = new Lease();
        lease.setLeaseId(generateLeaseId());
        lease.setApplicationId(application.getApplicationId());
        lease.setTenantId(application.getTenantId());
        lease.setPropertyId(application.getPropertyId());
        lease.setStartDate(start.toString());
        lease.setEndDate(end.toString());
        lease.setMonthlyRent(property.getMonthlyRent());
        lease.setSecurityDeposit(property.getSecurityDeposit());
        lease.setLeaseStatus("Active");

        Lease savedLease = leaseRepository.save(lease);

        property.setPropertyStatus("Rented");
        propertyRepository.save(property);

        return savedLease;
    }
}

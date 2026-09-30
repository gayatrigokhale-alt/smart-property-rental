package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.model.Application;
import com.rental.smart_property_rental.model.Lease;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.repository.ApplicationRepository;
import com.rental.smart_property_rental.repository.LeaseRepository;
import com.rental.smart_property_rental.repository.PropertyRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final ApplicationRepository applicationRepository;
    private final PropertyRepository propertyRepository;

    public LeaseService(
            LeaseRepository leaseRepository,
            ApplicationRepository applicationRepository,
            PropertyRepository propertyRepository) {

        this.leaseRepository = leaseRepository;
        this.applicationRepository = applicationRepository;
        this.propertyRepository = propertyRepository;
    }

    // CREATE LEASE
    public Lease createLease(Lease lease, String userId) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("User ID is required");
        }

        Application application =
                applicationRepository.findByApplicationId(
                        lease.getApplicationId()
                ).orElseThrow(() ->
                        new RuntimeException("Application not found"));

        // Lease can only be created for an approved application
        if (!"Approved".equals(application.getApplicationStatus())) {
            throw new RuntimeException(
                    "Lease can only be created for an approved application"
            );
        }

        Property property =
                propertyRepository.findByPropertyId(
                        application.getPropertyId()
                ).orElseThrow(() ->
                        new RuntimeException("Property not found"));

        // Only the landlord who owns the property can create the lease
        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to create this lease"
            );
        }

        // Prevent duplicate lease for the same application
        if (leaseRepository.findByApplicationId(
                application.getApplicationId()).isPresent()) {

            throw new RuntimeException(
                    "A lease already exists for this application"
            );
        }

        String leaseId = generateLeaseId();

        lease.setLeaseId(leaseId);

        lease.setTenantId(application.getTenantId());
        lease.setPropertyId(application.getPropertyId());

        lease.setLeaseStatus("Active");

        // If start date is not provided, use today's date
        if (lease.getStartDate() == null ||
                lease.getStartDate().isBlank()) {

            lease.setStartDate(LocalDate.now().toString());
        }

        Lease savedLease = leaseRepository.save(lease);

        return savedLease;
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
    public List<Lease> getLeasesByProperty(String propertyId) {

        return leaseRepository.findByPropertyId(propertyId);
    }

    // UPDATE LEASE
    public Lease updateLease(
            String leaseId,
            Lease updatedLease,
            String userId) {

        Lease existingLease =
                leaseRepository.findByLeaseId(leaseId)
                        .orElseThrow(() ->
                                new RuntimeException("Lease not found"));

        Property property =
                propertyRepository.findByPropertyId(
                        existingLease.getPropertyId()
                ).orElseThrow(() ->
                        new RuntimeException("Property not found"));

        // Only property owner can update lease
        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to update this lease"
            );
        }

        if (!"Active".equals(existingLease.getLeaseStatus())) {
            throw new RuntimeException(
                    "Only an active lease can be updated"
            );
        }

        existingLease.setStartDate(updatedLease.getStartDate());
        existingLease.setEndDate(updatedLease.getEndDate());
        existingLease.setMonthlyRent(updatedLease.getMonthlyRent());
        existingLease.setSecurityDeposit(updatedLease.getSecurityDeposit());

        return leaseRepository.save(existingLease);
    }

    // TERMINATE LEASE
    public Lease terminateLease(
            String leaseId,
            String userId) {

        Lease existingLease =
                leaseRepository.findByLeaseId(leaseId)
                        .orElseThrow(() ->
                                new RuntimeException("Lease not found"));

        Property property =
                propertyRepository.findByPropertyId(
                        existingLease.getPropertyId()
                ).orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to terminate this lease"
            );
        }

        if (!"Active".equals(existingLease.getLeaseStatus())) {
            throw new RuntimeException(
                    "Only an active lease can be terminated"
            );
        }

        existingLease.setLeaseStatus("Terminated");
        existingLease.setTerminationDate(
                LocalDate.now().toString()
        );

        return leaseRepository.save(existingLease);
    }

    // COMPLETE LEASE
    public Lease completeLease(
            String leaseId,
            String userId) {

        Lease existingLease =
                leaseRepository.findByLeaseId(leaseId)
                        .orElseThrow(() ->
                                new RuntimeException("Lease not found"));

        Property property =
                propertyRepository.findByPropertyId(
                        existingLease.getPropertyId()
                ).orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to complete this lease"
            );
        }

        if (!"Active".equals(existingLease.getLeaseStatus())) {
            throw new RuntimeException(
                    "Only an active lease can be completed"
            );
        }

        existingLease.setLeaseStatus("Completed");

        return leaseRepository.save(existingLease);
    }

    // GENERATE LEASE ID
    private String generateLeaseId() {

        long count = leaseRepository.count() + 1;

        String leaseId =
                String.format("LEASE%03d", count);

        while (leaseRepository.existsByLeaseId(leaseId)) {

            count++;

            leaseId =
                    String.format("LEASE%03d", count);
        }

        return leaseId;
    }
}
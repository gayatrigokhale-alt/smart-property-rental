package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.model.Application;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.repository.ApplicationRepository;
import com.rental.smart_property_rental.repository.PropertyRepository;
import com.rental.smart_property_rental.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            TenantRepository tenantRepository,
            PropertyRepository propertyRepository) {

        this.applicationRepository = applicationRepository;
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Application getApplicationById(String applicationId) {
        return applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));
    }

    public List<Application> getApplicationsByTenant(String tenantId) {

        if (!tenantRepository.existsByTenantId(tenantId)) {
            throw new RuntimeException("Tenant not found");
        }

        return applicationRepository.findByTenantId(tenantId);
    }

    public List<Application> getApplicationsByProperty(
            String propertyId,
            String userId) {

        Property property =
                propertyRepository.findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to view applications for this property"
            );
        }

        return applicationRepository.findByPropertyId(propertyId);
    }

    public Application createApplication(
            Application application,
            String userId) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("User ID is required");
        }

        if (!userId.equals(application.getTenantId())) {
            throw new RuntimeException(
                    "You can only create an application for your own tenant account"
            );
        }

        if (!tenantRepository.existsByTenantId(application.getTenantId())) {
            throw new RuntimeException("Tenant not found");
        }

        if (!propertyRepository.existsByPropertyId(application.getPropertyId())) {
            throw new RuntimeException("Property not found");
        }

        if (applicationRepository.existsByTenantIdAndPropertyId(
                application.getTenantId(),
                application.getPropertyId())) {

            throw new RuntimeException(
                    "You have already applied for this property"
            );
        }

        String applicationId = generateApplicationId();

        application.setApplicationId(applicationId);

        application.setApplicationDate(
                LocalDate.now().toString()
        );

        // Every newly created application starts as Pending.
        application.setApplicationStatus("Pending");

        return applicationRepository.save(application);
    }

    public Application updateApplicationStatus(
            String applicationId,
            String status,
            String userId) {

        Application existingApplication =
                applicationRepository.findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("Application not found"));

        Property property =
                propertyRepository.findByPropertyId(
                        existingApplication.getPropertyId()
                ).orElseThrow(() ->
                        new RuntimeException("Property not found"));

        // Only the landlord who owns the property
        // can approve or reject an application.
        if (!property.getLandlordId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to update this application"
            );
        }

        if (!status.equals("Pending")
                && !status.equals("Approved")
                && !status.equals("Rejected")
                && !status.equals("Withdrawn")) {

            throw new RuntimeException(
                    "Invalid application status"
            );
        }

        existingApplication.setApplicationStatus(status);

        return applicationRepository.save(existingApplication);
    }

    public Application withdrawApplication(
            String applicationId,
            String userId) {

        Application existingApplication =
                applicationRepository.findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("Application not found"));

        // Only the tenant who created the application
        // can withdraw it.
        if (!existingApplication.getTenantId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to withdraw this application"
            );
        }

        existingApplication.setApplicationStatus("Withdrawn");

        return applicationRepository.save(existingApplication);
    }

    public void deleteApplication(
            String applicationId,
            String userId) {

        Application existingApplication =
                applicationRepository.findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("Application not found"));

        // Tenant can delete their own application.
        if (existingApplication.getTenantId().equals(userId)) {
            applicationRepository.delete(existingApplication);
            return;
        }

        Property property =
                propertyRepository.findByPropertyId(
                        existingApplication.getPropertyId()
                ).orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (property.getLandlordId().equals(userId)) {
            applicationRepository.delete(existingApplication);
            return;
        }

        throw new RuntimeException(
                "You are not authorized to delete this application"
        );
    }

    private String generateApplicationId() {

        long count = applicationRepository.count() + 1;

        String applicationId =
                String.format("APP%03d", count);

        while (applicationRepository.existsByApplicationId(applicationId)) {
            count++;
            applicationId =
                    String.format("APP%03d", count);
        }

        return applicationId;
    }
}
package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.model.Amenity;
import com.rental.smart_property_rental.model.Tenant;
import com.rental.smart_property_rental.model.TenantAmenityPreference;
import com.rental.smart_property_rental.repository.AmenityRepository;
import com.rental.smart_property_rental.repository.TenantAmenityPreferenceRepository;
import com.rental.smart_property_rental.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TenantAmenityPreferenceService {

    private final TenantAmenityPreferenceRepository
            tenantAmenityPreferenceRepository;

    private final AmenityRepository amenityRepository;

    private final TenantRepository tenantRepository;

    public TenantAmenityPreferenceService(
            TenantAmenityPreferenceRepository tenantAmenityPreferenceRepository,
            AmenityRepository amenityRepository,
            TenantRepository tenantRepository) {

        this.tenantAmenityPreferenceRepository =
                tenantAmenityPreferenceRepository;

        this.amenityRepository =
                amenityRepository;

        this.tenantRepository =
                tenantRepository;
    }

    // Validate Tenant_ID
    private void validateTenantId(String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {

            throw new IllegalArgumentException(
                    "Tenant_ID is required"
            );
        }

        boolean tenantExists =
                tenantRepository.findAll().stream()
                        .anyMatch(tenant ->
                                tenantId.equals(tenant.getTenantId())
                        );

        if (!tenantExists) {

            throw new IllegalArgumentException(
                    "Tenant_ID is invalid"
            );
        }
    }

    // Validate Amenity_IDs
    private void validateAmenityIds(List<String> amenityIds) {

        if (amenityIds == null || amenityIds.isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one amenity must be selected"
            );
        }

        if (amenityIds.stream()
                .anyMatch(id -> id == null || id.isBlank())) {

            throw new IllegalArgumentException(
                    "Amenity_ID cannot be empty"
            );
        }

        // Remove duplicate IDs from the request
        List<String> uniqueAmenityIds =
                amenityIds.stream()
                        .distinct()
                        .toList();

        // Check all IDs in one database query
        List<Amenity> existingAmenities =
                amenityRepository.findByAmenityIdIn(
                        uniqueAmenityIds
                );

        // Compare the IDs that actually exist
        Set<String> foundAmenityIds =
                existingAmenities.stream()
                        .map(Amenity::getAmenityId)
                        .collect(Collectors.toSet());

        if (!foundAmenityIds.containsAll(uniqueAmenityIds)) {

            throw new IllegalArgumentException(
                    "One or more Amenity_IDs are invalid"
            );
        }
    }

    // GET
    public Optional<TenantAmenityPreference> getPreferencesByTenantId(
            String tenantId) {

        return tenantAmenityPreferenceRepository
                .findByTenantId(tenantId);
    }

    // CREATE
    public TenantAmenityPreference createPreferences(
            TenantAmenityPreference preference) {

        // First check whether Tenant_ID is valid
        validateTenantId(
                preference.getTenantId()
        );

        // Check that tenant doesn't already have preferences
        if (tenantAmenityPreferenceRepository
                .findByTenantId(preference.getTenantId())
                .isPresent()) {

            throw new RuntimeException(
                    "Amenity preferences already exist for tenant: "
                            + preference.getTenantId()
            );
        }

        // Check that all Amenity_IDs are valid
        validateAmenityIds(
                preference.getAmenityIds()
        );

        // Remove duplicate IDs before saving
        preference.setAmenityIds(
                preference.getAmenityIds()
                        .stream()
                        .distinct()
                        .toList()
        );

        return tenantAmenityPreferenceRepository
                .save(preference);
    }

    // UPDATE
    public TenantAmenityPreference updatePreferences(
            String id,
            TenantAmenityPreference updatedPreference) {

        TenantAmenityPreference existingPreference =
                tenantAmenityPreferenceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Amenity preferences not found with ID: "
                                                + id
                                )
                        );

        // Validate Tenant_ID
        validateTenantId(
                updatedPreference.getTenantId()
        );

        // Validate all new Amenity_IDs
        validateAmenityIds(
                updatedPreference.getAmenityIds()
        );

        // Update Tenant_ID
        existingPreference.setTenantId(
                updatedPreference.getTenantId()
        );

        // Update with unique Amenity_IDs
        existingPreference.setAmenityIds(
                updatedPreference.getAmenityIds()
                        .stream()
                        .distinct()
                        .toList()
        );

        return tenantAmenityPreferenceRepository
                .save(existingPreference);
    }

    // DELETE
    public void deletePreferences(String id) {

        if (!tenantAmenityPreferenceRepository
                .existsById(id)) {

            throw new RuntimeException(
                    "Amenity preferences not found with ID: "
                            + id
            );
        }

        tenantAmenityPreferenceRepository
                .deleteById(id);
    }
}


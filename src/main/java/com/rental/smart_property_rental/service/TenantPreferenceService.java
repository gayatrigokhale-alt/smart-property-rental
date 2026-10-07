package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.model.TenantPreference;
import com.rental.smart_property_rental.repository.TenantPreferenceRepository;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantPreferenceService {

    private final TenantPreferenceRepository tenantPreferenceRepository;

    public TenantPreferenceService(
            TenantPreferenceRepository tenantPreferenceRepository) {

        this.tenantPreferenceRepository = tenantPreferenceRepository;
    }

    // GET
    public Optional<TenantPreference> getPreferencesByTenantId(
            String tenantId) {

        return tenantPreferenceRepository.findByTenantId(tenantId);
    }

    // CREATE
    public TenantPreference createPreferences(
            TenantPreference preference) {

        validateBudgetRange(preference);

        // A tenant can have only one preference document
        if (tenantPreferenceRepository
                .findByTenantId(preference.getTenantId())
                .isPresent()) {

            throw new RuntimeException(
                    "Preferences already exist for tenant: "
                            + preference.getTenantId()
            );
        }

        // Preference_ID must be entered
        if (preference.getPreferenceId() == null ||
                preference.getPreferenceId().isBlank()) {

            throw new RuntimeException(
                    "Preference_ID is required"
            );
        }

        return tenantPreferenceRepository.save(preference);
    }

    // UPDATE
    public TenantPreference updatePreferences(
            String id,
            TenantPreference updatedPreference) {

        TenantPreference existingPreference =
                tenantPreferenceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Preferences not found"
                                )
                        );

        validateBudgetRange(updatedPreference);

        // Keep existing Preference_ID
        existingPreference.setPreferenceId(
                existingPreference.getPreferenceId()
        );

        // Keep existing Tenant_ID
        existingPreference.setTenantId(
                existingPreference.getTenantId()
        );

        existingPreference.setPreferredLocation(
                updatedPreference.getPreferredLocation()
        );

        existingPreference.setMinimumBudget(
                updatedPreference.getMinimumBudget()
        );

        existingPreference.setMaximumBudget(
                updatedPreference.getMaximumBudget()
        );

        existingPreference.setPreferredPropertyType(
                updatedPreference.getPreferredPropertyType()
        );

        existingPreference.setPreferredBhk(
                updatedPreference.getPreferredBhk()
        );

        existingPreference.setMinimumAreaSqft(
                updatedPreference.getMinimumAreaSqft()
        );

        existingPreference.setMinimumBathrooms(
                updatedPreference.getMinimumBathrooms()
        );

        existingPreference.setNumberOfOccupants(
                updatedPreference.getNumberOfOccupants()
        );

        existingPreference.setFurnishingPreference(
                updatedPreference.getFurnishingPreference()
        );

        existingPreference.setPreferredFloor(
                updatedPreference.getPreferredFloor()
        );

        existingPreference.setMinimumLeaseDuration(
                updatedPreference.getMinimumLeaseDuration()
        );

        existingPreference.setPreferredMoveInDate(
                updatedPreference.getPreferredMoveInDate()
        );

        return tenantPreferenceRepository.save(existingPreference);
    }

    // DELETE
    public void deletePreferences(String id) {

        if (!tenantPreferenceRepository.existsById(id)) {
            throw new RuntimeException(
                    "Preferences not found"
            );
        }

        tenantPreferenceRepository.deleteById(id);
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateBudgetRange(
            TenantPreference preference) {

        if (preference.getMinimumBudget() == null ||
                preference.getMaximumBudget() == null) {

            return;
        }

        if (preference.getMinimumBudget()
                > preference.getMaximumBudget()) {

            throw new IllegalArgumentException(
                    "Minimum budget cannot be greater than maximum budget"
            );
        }
    }
}
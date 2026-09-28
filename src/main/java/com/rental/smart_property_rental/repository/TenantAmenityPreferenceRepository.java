package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.TenantAmenityPreference;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TenantAmenityPreferenceRepository
        extends MongoRepository<TenantAmenityPreference, String> {

    Optional<TenantAmenityPreference> findByTenantId(String tenantId);
}
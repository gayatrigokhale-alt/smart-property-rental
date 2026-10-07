package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.TenantPreference;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TenantPreferenceRepository
        extends MongoRepository<TenantPreference, String> {

    Optional<TenantPreference> findByTenantId(String tenantId);

    void deleteByTenantId(String tenantId);
}
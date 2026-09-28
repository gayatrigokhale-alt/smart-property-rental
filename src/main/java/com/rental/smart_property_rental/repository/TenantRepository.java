package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TenantRepository extends MongoRepository<Tenant, String> {

    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findByTenantId(String tenantId);
}
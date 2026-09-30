package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository
        extends MongoRepository<Application, String> {

    Optional<Application> findByApplicationId(String applicationId);

    boolean existsByApplicationId(String applicationId);

    List<Application> findByTenantId(String tenantId);

    List<Application> findByPropertyId(String propertyId);

    boolean existsByTenantIdAndPropertyId(String tenantId, String propertyId);
}
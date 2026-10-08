package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.PropertyAmenity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PropertyAmenityRepository
        extends MongoRepository<PropertyAmenity, String> {

    Optional<PropertyAmenity> findByPropertyId(String propertyId);
    boolean existsByPropertyId(String propertyId);
}
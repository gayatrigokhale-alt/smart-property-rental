
        package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.Property;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository
        extends MongoRepository<Property, String> {

    Optional<Property> findByPropertyId(String propertyId);

    boolean existsByPropertyId(String propertyId);

    List<Property> findByLandlordId(String landlordId);

    // Get only properties whose status is Available
    List<Property> findByPropertyStatus(String propertyStatus);

}



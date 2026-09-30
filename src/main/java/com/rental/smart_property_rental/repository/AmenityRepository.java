package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.Amenity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AmenityRepository
        extends MongoRepository<Amenity, String> {

    Optional<Amenity> findByAmenityId(String amenityId);

    List<Amenity> findByAmenityIdIn(List<String> amenityIds);
    boolean existsByAmenityId(String amenityId);
}
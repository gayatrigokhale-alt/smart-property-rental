package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.Landlord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LandlordRepository extends MongoRepository<Landlord, String> {

    Optional<Landlord> findByEmail(String email);

    Optional<Landlord> findByLandlordId(String landlordId);
}
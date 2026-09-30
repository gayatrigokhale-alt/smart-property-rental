package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.exception.InvalidRequestException;
import com.rental.smart_property_rental.exception.UnauthorizedActionException;
import com.rental.smart_property_rental.model.PropertyAmenity;
import com.rental.smart_property_rental.repository.AmenityRepository;
import com.rental.smart_property_rental.repository.PropertyAmenityRepository;
import com.rental.smart_property_rental.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyAmenityService {

    private final PropertyAmenityRepository propertyAmenityRepository;
    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;

    public PropertyAmenityService(
            PropertyAmenityRepository propertyAmenityRepository,
            PropertyRepository propertyRepository,
            AmenityRepository amenityRepository) {

        this.propertyAmenityRepository = propertyAmenityRepository;
        this.propertyRepository = propertyRepository;
        this.amenityRepository = amenityRepository;
    }

    public List<PropertyAmenity> getAllPropertyAmenities() {
        return propertyAmenityRepository.findAll();
    }

    public PropertyAmenity getPropertyAmenities(String propertyId) {

        return propertyAmenityRepository.findByPropertyId(propertyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Property amenities not found"
                        ));
    }

    public PropertyAmenity createPropertyAmenities(
            PropertyAmenity propertyAmenity,
            String userId) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("User ID is required");
        }

        if (!propertyRepository.existsByPropertyId(
                propertyAmenity.getPropertyId())) {

            throw new RuntimeException("Property not found");
        }

        var property = propertyRepository
                .findByPropertyId(propertyAmenity.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new UnauthorizedActionException(
                    "You can only manage amenities for your own property"
            );
        }

        if (propertyAmenityRepository.existsByPropertyId(
                propertyAmenity.getPropertyId())) {

            throw new RuntimeException(
                    "Amenities already exist for this property"
            );
        }

        validateAmenities(propertyAmenity.getAmenityIds());

        return propertyAmenityRepository.save(propertyAmenity);
    }

    public PropertyAmenity updatePropertyAmenities(
            String propertyId,
            PropertyAmenity updatedPropertyAmenity,
            String userId) {

        var property = propertyRepository
                .findByPropertyId(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to modify amenities for this property"
            );
        }

        PropertyAmenity existing =
                propertyAmenityRepository.findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property amenities not found"
                                ));

        validateAmenities(updatedPropertyAmenity.getAmenityIds());

        existing.setAmenityIds(
                updatedPropertyAmenity.getAmenityIds()
        );

        return propertyAmenityRepository.save(existing);
    }

    public void deletePropertyAmenities(
            String propertyId,
            String userId) {

        var property = propertyRepository
                .findByPropertyId(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getLandlordId().equals(userId)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to delete amenities for this property"
            );
        }

        PropertyAmenity existing =
                propertyAmenityRepository.findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property amenities not found"
                                ));

        propertyAmenityRepository.delete(existing);
    }

    private void validateAmenities(List<String> amenityIds) {

        if (amenityIds == null || amenityIds.isEmpty()) {
            throw new InvalidRequestException(
                    "At least one amenity is required"
            );
        }

        for (String amenityId : amenityIds) {

            if (!amenityRepository.existsByAmenityId(amenityId)) {
                throw new InvalidRequestException(
                        "Amenity not found: " + amenityId
                );
            }
        }
    }
}
package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    // Get all properties
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    // Get property by Property_ID
    public Optional<Property> getPropertyByPropertyId(String propertyId) {
        return propertyRepository.findByPropertyId(propertyId);
    }

    // Get properties belonging to a landlord
    public List<Property> getPropertiesByLandlord(String landlordId) {
        return propertyRepository.findByLandlordId(landlordId);
    }

    // Create property
    public Property createProperty(
            Property property,
            String userId) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("X-User-Id header is required");
        }

        if (!userId.equals(property.getLandlordId())) {
            throw new RuntimeException(
                    "You can only create a property for your own landlord account"
            );
        }

        if (property.getPropertyId() == null ||
                property.getPropertyId().isBlank()) {
            throw new RuntimeException("Property_ID is required");
        }

        if (propertyRepository
                .existsByPropertyId(property.getPropertyId())) {
            throw new RuntimeException("Property_ID already exists");
        }

        return propertyRepository.save(property);
    }

    // Update property
    public Property updateProperty(
            String propertyId,
            Property updatedProperty,
            String userId) {

        Property existingProperty =
                propertyRepository.findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"));

        // Ownership check
        if (!userId.equals(existingProperty.getLandlordId())) {
            throw new RuntimeException(
                    "You are not authorized to modify this property"
            );
        }

        // Property_ID cannot be changed
        existingProperty.setPropertyId(
                existingProperty.getPropertyId()
        );

        // Landlord_ID cannot be changed
        existingProperty.setLandlordId(
                existingProperty.getLandlordId()
        );

        existingProperty.setPropertyType(
                updatedProperty.getPropertyType()
        );

        existingProperty.setLocation(
                updatedProperty.getLocation()
        );

        existingProperty.setCity(
                updatedProperty.getCity()
        );

        existingProperty.setPincode(
                updatedProperty.getPincode()
        );

        existingProperty.setBhk(
                updatedProperty.getBhk()
        );

        existingProperty.setAreaSqft(
                updatedProperty.getAreaSqft()
        );

        existingProperty.setNumberOfBathrooms(
                updatedProperty.getNumberOfBathrooms()
        );

        existingProperty.setMonthlyRent(
                updatedProperty.getMonthlyRent()
        );

        existingProperty.setSecurityDeposit(
                updatedProperty.getSecurityDeposit()
        );

        existingProperty.setFloorNumber(
                updatedProperty.getFloorNumber()
        );

        existingProperty.setFurnishingStatus(
                updatedProperty.getFurnishingStatus()
        );

        existingProperty.setAvailableFrom(
                updatedProperty.getAvailableFrom()
        );

        existingProperty.setPropertyStatus(
                updatedProperty.getPropertyStatus()
        );

        return propertyRepository.save(existingProperty);
    }

    // Delete property
    public void deleteProperty(
            String propertyId,
            String userId) {

        Property existingProperty =
                propertyRepository.findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"));

        // Ownership check
        if (!userId.equals(existingProperty.getLandlordId())) {
            throw new RuntimeException(
                    "You are not authorized to delete this property"
            );
        }

        propertyRepository.delete(existingProperty);
    }
}
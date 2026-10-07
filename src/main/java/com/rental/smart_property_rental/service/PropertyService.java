package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.dto.PropertyResponse;
import com.rental.smart_property_rental.model.Amenity;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.model.PropertyAmenity;
import com.rental.smart_property_rental.repository.AmenityRepository;
import com.rental.smart_property_rental.repository.LeaseRepository;
import com.rental.smart_property_rental.repository.PropertyAmenityRepository;
import com.rental.smart_property_rental.repository.PropertyRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyAmenityRepository propertyAmenityRepository;
    private final AmenityRepository amenityRepository;
    private final LeaseRepository leaseRepository;

    public PropertyService(
            PropertyRepository propertyRepository,
            PropertyAmenityRepository propertyAmenityRepository,
            AmenityRepository amenityRepository,
            LeaseRepository leaseRepository) {

        this.propertyRepository = propertyRepository;
        this.propertyAmenityRepository = propertyAmenityRepository;
        this.amenityRepository = amenityRepository;
        this.leaseRepository = leaseRepository;
    }

    // =====================================================
    // GET ALL PROPERTIES
    // =====================================================

    public List<Property> getAllProperties() {

        return propertyRepository.findAll();
    }

    // =====================================================
    // GET ONLY AVAILABLE PROPERTIES WITH AMENITIES
    // =====================================================

    public List<PropertyResponse> getAvailableProperties() {

        List<Property> properties =
                propertyRepository.findByPropertyStatus("Available");

        return properties.stream()
                .map(this::createPropertyResponse)
                .toList();
    }

    // =====================================================
    // GET PROPERTY BY PROPERTY_ID
    // =====================================================

    public Optional<Property> getPropertyByPropertyId(
            String propertyId) {

        return propertyRepository.findByPropertyId(propertyId);
    }

    // =====================================================
    // GET PROPERTIES BELONGING TO A LANDLORD
    // =====================================================

    public List<Property> getPropertiesByLandlord(
            String landlordId) {

        return propertyRepository.findByLandlordId(landlordId);
    }

    // =====================================================
    // CREATE PROPERTY
    // =====================================================

    public Property createProperty(
            Property property,
            String userId) {

        if (userId == null || userId.isBlank()) {

            throw new RuntimeException(
                    "X-User-Id header is required"
            );
        }

        if (!userId.equals(property.getLandlordId())) {

            throw new RuntimeException(
                    "You can only create a property for your own landlord account"
            );
        }

        if (property.getPropertyId() == null ||
                property.getPropertyId().isBlank()) {

            throw new RuntimeException(
                    "Property_ID is required"
            );
        }

        if (propertyRepository
                .existsByPropertyId(property.getPropertyId())) {

            throw new RuntimeException(
                    "Property_ID already exists"
            );
        }

        return propertyRepository.save(property);
    }

    // =====================================================
    // UPDATE PROPERTY
    // =====================================================

    public Property updateProperty(
            String propertyId,
            Property updatedProperty,
            String userId) {

        Property existingProperty =
                propertyRepository.findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

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

    // =====================================================
    // DELETE PROPERTY
    // =====================================================

    public void deleteProperty(
            String propertyId,
            String userId) {

        Property existingProperty =
                propertyRepository.findByPropertyId(propertyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Property not found"
                                ));

        // Ownership check
        if (!userId.equals(existingProperty.getLandlordId())) {

            throw new RuntimeException(
                    "You are not authorized to delete this property"
            );
        }

        // Check whether the property has an active lease
        boolean hasActiveLease =
                leaseRepository.findByPropertyId(propertyId)
                        .stream()
                        .anyMatch(lease ->
                                "Active".equalsIgnoreCase(
                                        lease.getLeaseStatus()
                                )
                        );

        if (hasActiveLease) {

            throw new RuntimeException(
                    "Property cannot be deleted while it has an active lease"
            );
        }

        // Delete property amenities first
        propertyAmenityRepository
                .findByPropertyId(propertyId)
                .ifPresent(propertyAmenity ->
                        propertyAmenityRepository.delete(propertyAmenity)
                );

        // Delete property
        propertyRepository.delete(existingProperty);
    }

    // =====================================================
    // CREATE PROPERTY RESPONSE WITH AMENITY NAMES
    // =====================================================

    private PropertyResponse createPropertyResponse(
            Property property) {

        PropertyResponse response =
                new PropertyResponse();

        response.setPropertyId(
                property.getPropertyId()
        );

        response.setLandlordId(
                property.getLandlordId()
        );

        response.setPropertyType(
                property.getPropertyType()
        );

        response.setLocation(
                property.getLocation()
        );

        response.setCity(
                property.getCity()
        );

        response.setPincode(
                property.getPincode()
        );

        response.setBhk(
                property.getBhk()
        );

        response.setAreaSqft(
                property.getAreaSqft()
        );

        response.setNumberOfBathrooms(
                property.getNumberOfBathrooms()
        );

        response.setMonthlyRent(
                property.getMonthlyRent()
        );

        response.setSecurityDeposit(
                property.getSecurityDeposit()
        );

        response.setFloorNumber(
                property.getFloorNumber()
        );

        response.setFurnishingStatus(
                property.getFurnishingStatus()
        );

        response.setAvailableFrom(
                property.getAvailableFrom()
        );

        response.setPropertyStatus(
                property.getPropertyStatus()
        );

        // Find amenities for this property
        Optional<PropertyAmenity> propertyAmenity =
                propertyAmenityRepository
                        .findByPropertyId(property.getPropertyId());

        List<String> amenityNames =
                new ArrayList<>();

        if (propertyAmenity.isPresent()) {

            List<String> amenityIds =
                    propertyAmenity.get().getAmenityIds();

            if (amenityIds != null &&
                    !amenityIds.isEmpty()) {

                List<Amenity> amenities =
                        amenityRepository
                                .findByAmenityIdIn(amenityIds);

                for (Amenity amenity : amenities) {

                    amenityNames.add(
                            amenity.getAmenityName()
                    );
                }
            }
        }

        response.setAmenities(amenityNames);

        return response;
    }
}
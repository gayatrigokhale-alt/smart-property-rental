package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.dto.PropertyResponse;
import com.rental.smart_property_rental.model.Amenity;
import com.rental.smart_property_rental.model.Landlord;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.model.PropertyAmenity;
import com.rental.smart_property_rental.model.TenantAmenityPreference;
import com.rental.smart_property_rental.model.TenantPreference;
import com.rental.smart_property_rental.repository.AmenityRepository;
import com.rental.smart_property_rental.repository.LandlordRepository;
import com.rental.smart_property_rental.repository.PropertyAmenityRepository;
import com.rental.smart_property_rental.repository.TenantAmenityPreferenceRepository;
import com.rental.smart_property_rental.repository.TenantPreferenceRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PropertySearchService {

    private final MongoTemplate mongoTemplate;
    private final TenantPreferenceRepository tenantPreferenceRepository;
    private final TenantAmenityPreferenceRepository tenantAmenityPreferenceRepository;
    private final PropertyAmenityRepository propertyAmenityRepository;
    private final AmenityRepository amenityRepository;
    private final LandlordRepository landlordRepository;

    public PropertySearchService(
            MongoTemplate mongoTemplate,
            TenantPreferenceRepository tenantPreferenceRepository,
            TenantAmenityPreferenceRepository tenantAmenityPreferenceRepository,
            PropertyAmenityRepository propertyAmenityRepository,
            AmenityRepository amenityRepository,
            LandlordRepository landlordRepository) {

        this.mongoTemplate = mongoTemplate;
        this.tenantPreferenceRepository = tenantPreferenceRepository;
        this.tenantAmenityPreferenceRepository =
                tenantAmenityPreferenceRepository;
        this.propertyAmenityRepository =
                propertyAmenityRepository;
        this.amenityRepository =
                amenityRepository;
        this.landlordRepository =
                landlordRepository;
    }

    public List<PropertyResponse> searchProperties(String tenantId) {

        TenantPreference preference =
                tenantPreferenceRepository.findByTenantId(tenantId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Preferences not found for tenant: "
                                                + tenantId
                                ));

        List<String> requiredAmenityIds =
                tenantAmenityPreferenceRepository
                        .findByTenantId(tenantId)
                        .map(TenantAmenityPreference::getAmenityIds)
                        .orElse(Collections.emptyList());

        Query query = new Query();

        // Only available properties
        query.addCriteria(
                Criteria.where("Property_Status")
                        .is("Available")
        );

        // Budget
        if (preference.getMinimumBudget() != null &&
                preference.getMaximumBudget() != null) {

            query.addCriteria(
                    Criteria.where("Monthly_Rent")
                            .gte(preference.getMinimumBudget())
                            .lte(preference.getMaximumBudget())
            );

        } else if (preference.getMinimumBudget() != null) {

            query.addCriteria(
                    Criteria.where("Monthly_Rent")
                            .gte(preference.getMinimumBudget())
            );

        } else if (preference.getMaximumBudget() != null) {

            query.addCriteria(
                    Criteria.where("Monthly_Rent")
                            .lte(preference.getMaximumBudget())
            );
        }

        // Property type
        if (isNotBlank(preference.getPreferredPropertyType())) {

            query.addCriteria(
                    Criteria.where("Property_Type")
                            .is(preference.getPreferredPropertyType())
            );
        }

        // BHK
        if (preference.getPreferredBhk() != null) {

            query.addCriteria(
                    Criteria.where("BHK")
                            .is(preference.getPreferredBhk())
            );
        }

        // Location
        if (isNotBlank(preference.getPreferredLocation())) {

            query.addCriteria(
                    Criteria.where("Location")
                            .is(preference.getPreferredLocation())
            );
        }

        // Minimum area
        if (preference.getMinimumAreaSqft() != null) {

            query.addCriteria(
                    Criteria.where("Area_sqft")
                            .gte(preference.getMinimumAreaSqft())
            );
        }

        // Minimum bathrooms
        if (preference.getMinimumBathrooms() != null) {

            query.addCriteria(
                    Criteria.where("Number_of_Bathrooms")
                            .gte(preference.getMinimumBathrooms())
            );
        }

        // Furnishing
        if (isNotBlank(preference.getFurnishingPreference())) {

            query.addCriteria(
                    Criteria.where("Furnishing_Status")
                            .is(preference.getFurnishingPreference())
            );
        }

        // Floor preference
        addFloorCriteria(
                query,
                preference.getPreferredFloor()
        );

        // Move-in date
        if (isNotBlank(preference.getPreferredMoveInDate())) {

            query.addCriteria(
                    Criteria.where("Available_From")
                            .lte(preference.getPreferredMoveInDate())
            );
        }

        // First filter properties using PROPERTY collection
        List<Property> properties =
                mongoTemplate.find(query, Property.class);

        /*
         * Only show properties belonging to VERIFIED landlords.
         */
        properties = properties.stream()
                .filter(this::isPropertyLandlordVerified)
                .toList();

        // Filter properties according to required amenities
        if (requiredAmenityIds != null &&
                !requiredAmenityIds.isEmpty()) {

            properties = properties.stream()
                    .filter(property ->
                            hasAllRequiredAmenities(
                                    property.getPropertyId(),
                                    requiredAmenityIds
                            ))
                    .toList();
        }

        // Convert matching properties to PropertyResponse
        return properties.stream()
                .map(this::createPropertyResponse)
                .toList();
    }

    private boolean isPropertyLandlordVerified(
            Property property) {

        if (property.getLandlordId() == null ||
                property.getLandlordId().isBlank()) {

            return false;
        }

        return landlordRepository
                .findByLandlordId(property.getLandlordId())
                .map(landlord ->
                        "Verified".equalsIgnoreCase(
                                landlord.getVerificationStatus()
                        ))
                .orElse(false);
    }

    private boolean hasAllRequiredAmenities(
            String propertyId,
            List<String> requiredAmenityIds) {

        return propertyAmenityRepository
                .findByPropertyId(propertyId)
                .map(propertyAmenity -> {

                    Set<String> propertyAmenities =
                            new HashSet<>(
                                    propertyAmenity.getAmenityIds()
                            );

                    return propertyAmenities
                            .containsAll(requiredAmenityIds);
                })
                .orElse(false);
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
        List<String> amenityNames =
                new ArrayList<>();

        propertyAmenityRepository
                .findByPropertyId(property.getPropertyId())
                .ifPresent(propertyAmenity -> {

                    List<String> amenityIds =
                            propertyAmenity.getAmenityIds();

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
                });

        response.setAmenities(amenityNames);

        return response;
    }

    private void addFloorCriteria(
            Query query,
            String preferredFloor) {

        if (!isNotBlank(preferredFloor)) {
            return;
        }

        String floor =
                preferredFloor.trim().toLowerCase();

        switch (floor) {

            case "ground":

                query.addCriteria(
                        Criteria.where("Floor_Number")
                                .is(0)
                );
                break;

            case "low":

                query.addCriteria(
                        Criteria.where("Floor_Number")
                                .lte(3)
                );
                break;

            case "high":

                query.addCriteria(
                        Criteria.where("Floor_Number")
                                .gte(5)
                );
                break;

            case "any":
            case "any floor":

                // No floor restriction
                break;

            default:

                // Unknown floor preference:
                // do not apply a floor filter.
                break;
        }
    }

    private boolean isNotBlank(String value) {

        return value != null &&
                !value.isBlank();
    }
}
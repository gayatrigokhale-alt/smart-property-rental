package com.rental.smart_property_rental.dto;

import java.util.List;

public class PropertyResponse {

    private String propertyId;
    private String landlordId;
    private String propertyType;
    private String location;
    private String city;
    private String pincode;

    private Integer bhk;
    private Integer areaSqft;
    private Integer numberOfBathrooms;

    private Integer monthlyRent;
    private Integer securityDeposit;

    private Integer floorNumber;
    private String furnishingStatus;
    private String availableFrom;
    private String propertyStatus;

    private List<String> amenities;


    // Property ID
    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }


    // Landlord ID
    public String getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(String landlordId) {
        this.landlordId = landlordId;
    }


    // Property Type
    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }


    // Location
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    // City
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    // Pincode
    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }


    // BHK
    public Integer getBhk() {
        return bhk;
    }

    public void setBhk(Integer bhk) {
        this.bhk = bhk;
    }


    // Area
    public Integer getAreaSqft() {
        return areaSqft;
    }

    public void setAreaSqft(Integer areaSqft) {
        this.areaSqft = areaSqft;
    }


    // Number of Bathrooms
    public Integer getNumberOfBathrooms() {
        return numberOfBathrooms;
    }

    public void setNumberOfBathrooms(Integer numberOfBathrooms) {
        this.numberOfBathrooms = numberOfBathrooms;
    }


    // Monthly Rent
    public Integer getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(Integer monthlyRent) {
        this.monthlyRent = monthlyRent;
    }


    // Security Deposit
    public Integer getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(Integer securityDeposit) {
        this.securityDeposit = securityDeposit;
    }


    // Floor Number
    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }


    // Furnishing Status
    public String getFurnishingStatus() {
        return furnishingStatus;
    }

    public void setFurnishingStatus(String furnishingStatus) {
        this.furnishingStatus = furnishingStatus;
    }


    // Available From
    public String getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(String availableFrom) {
        this.availableFrom = availableFrom;
    }


    // Property Status
    public String getPropertyStatus() {
        return propertyStatus;
    }

    public void setPropertyStatus(String propertyStatus) {
        this.propertyStatus = propertyStatus;
    }


    // Amenities
    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }
}
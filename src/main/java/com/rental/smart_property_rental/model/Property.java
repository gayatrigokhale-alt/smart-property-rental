package com.rental.smart_property_rental.model;

import java.util.List;
import java.util.ArrayList;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "PROPERTY")
public class Property {

    @Id
    private String id;

    @Field("Property_ID")
    @NotBlank(message = "Property_ID is required")
    private String propertyId;

    @Field("Landlord_ID")
    @NotBlank(message = "Landlord_ID is required")
    private String landlordId;

    @Field("Property_Type")
    @NotBlank(message = "Property type is required")
    private String propertyType;

    @Field("Location")
    @NotBlank(message = "Location is required")
    private String location;

    @Field("City")
    @NotBlank(message = "City is required")
    private String city;

    @Field("Pincode")
    @NotBlank(message = "Pincode is required")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Enter a valid 6-digit pincode"
    )
    private String pincode;

    @Field("BHK")
    @NotNull(message = "BHK is required")
    @Min(value = 1, message = "BHK must be at least 1")
    private Integer bhk;

    @Field("Area_sqft")
    @NotNull(message = "Area is required")
    @Min(value = 1, message = "Area must be greater than 0")
    private Integer areaSqft;

    @Field("Number_of_Bathrooms")
    @NotNull(message = "Number of bathrooms is required")
    @Min(value = 1, message = "Number of bathrooms must be at least 1")
    private Integer numberOfBathrooms;

    @Field("Monthly_Rent")
    @NotNull(message = "Monthly rent is required")
    @Min(value = 1, message = "Monthly rent must be greater than 0")
    private Integer monthlyRent;

    @Field("Security_Deposit")
    @NotNull(message = "Security deposit is required")
    @Min(value = 0, message = "Security deposit cannot be negative")
    private Integer securityDeposit;

    @Field("Floor_Number")
    @NotNull(message = "Floor number is required")
    @Min(value = 0, message = "Floor number cannot be negative")
    private Integer floorNumber;

    @Field("Furnishing_Status")
    @NotBlank(message = "Furnishing status is required")
    private String furnishingStatus;

    @Field("Available_From")
    @NotBlank(message = "Available from date is required")
    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "Available from must be in YYYY-MM-DD format"
    )
    private String availableFrom;

    @Field("Property_Status")
    @NotBlank(message = "Property status is required")
    private String propertyStatus;
    @Field("Image_File_Ids")
    private List<String> imageFileIds = new ArrayList<>();

    @Field("Video_File_Id")
    private String videoFileId;

    public Property() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(String landlordId) {
        this.landlordId = landlordId;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Integer getBhk() {
        return bhk;
    }

    public void setBhk(Integer bhk) {
        this.bhk = bhk;
    }

    public Integer getAreaSqft() {
        return areaSqft;
    }

    public void setAreaSqft(Integer areaSqft) {
        this.areaSqft = areaSqft;
    }

    public Integer getNumberOfBathrooms() {
        return numberOfBathrooms;
    }

    public void setNumberOfBathrooms(Integer numberOfBathrooms) {
        this.numberOfBathrooms = numberOfBathrooms;
    }

    public Integer getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(Integer monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public Integer getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(Integer securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getFurnishingStatus() {
        return furnishingStatus;
    }

    public void setFurnishingStatus(String furnishingStatus) {
        this.furnishingStatus = furnishingStatus;
    }

    public String getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(String availableFrom) {
        this.availableFrom = availableFrom;
    }

    public String getPropertyStatus() {
        return propertyStatus;
    }

    public void setPropertyStatus(String propertyStatus) {
        this.propertyStatus = propertyStatus;
    }
    public List<String> getImageFileIds() {
        return imageFileIds;
    }

    public void setImageFileIds(List<String> imageFileIds) {
        this.imageFileIds = imageFileIds;
    }

    public String getVideoFileId() {
        return videoFileId;
    }

    public void setVideoFileId(String videoFileId) {
        this.videoFileId = videoFileId;
    }
}
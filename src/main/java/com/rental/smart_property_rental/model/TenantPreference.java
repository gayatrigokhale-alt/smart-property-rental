package com.rental.smart_property_rental.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "TENANT_PREFERENCE")
public class TenantPreference {

    @Id
    private String id;

    @Field("Preference_ID")
    @NotBlank(message = "Preference_ID is required")
    private String preferenceId;

    @Field("Tenant_ID")
    @NotBlank(message = "Tenant_ID is required")
    private String tenantId;

    @Field("Preferred_Location")
    @NotBlank(message = "Preferred location is required")
    private String preferredLocation;

    @Field("Minimum_Budget")
    @NotNull(message = "Minimum budget is required")
    @Min(value = 1, message = "Minimum budget must be greater than 0")
    private Integer minimumBudget;

    @Field("Maximum_Budget")
    @NotNull(message = "Maximum budget is required")
    @Min(value = 1, message = "Maximum budget must be greater than 0")
    private Integer maximumBudget;

    @Field("Preferred_Property_Type")
    @NotBlank(message = "Preferred property type is required")
    private String preferredPropertyType;

    @Field("Preferred_BHK")
    @NotNull(message = "Preferred BHK is required")
    @Min(value = 1, message = "Preferred BHK must be at least 1")
    private Integer preferredBhk;

    @Field("Minimum_Area_sqft")
    @NotNull(message = "Minimum area is required")
    @Min(value = 1, message = "Minimum area must be greater than 0")
    private Integer minimumAreaSqft;

    @Field("Minimum_Bathrooms")
    @NotNull(message = "Minimum bathrooms is required")
    @Min(value = 1, message = "Minimum bathrooms must be at least 1")
    private Integer minimumBathrooms;

    @Field("Number_of_Occupants")
    @NotNull(message = "Number of occupants is required")
    @Min(value = 1, message = "Number of occupants must be at least 1")
    private Integer numberOfOccupants;

    @Field("Furnishing_Preference")
    @NotBlank(message = "Furnishing preference is required")
    private String furnishingPreference;

    @Field("Preferred_Floor")
    @NotBlank(message = "Preferred floor is required")
    private String preferredFloor;

    @Field("Minimum_Lease_Duration")
    @NotNull(message = "Minimum lease duration is required")
    @Min(value = 1, message = "Minimum lease duration must be at least 1 month")
    private Integer minimumLeaseDuration;

    @Field("Preferred_Move_In_Date")
    @NotBlank(message = "Preferred move-in date is required")
    private String preferredMoveInDate;


    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public Integer getMinimumBudget() {
        return minimumBudget;
    }

    public void setMinimumBudget(Integer minimumBudget) {
        this.minimumBudget = minimumBudget;
    }

    public Integer getMaximumBudget() {
        return maximumBudget;
    }

    public void setMaximumBudget(Integer maximumBudget) {
        this.maximumBudget = maximumBudget;
    }

    public String getPreferredPropertyType() {
        return preferredPropertyType;
    }

    public void setPreferredPropertyType(String preferredPropertyType) {
        this.preferredPropertyType = preferredPropertyType;
    }

    public Integer getPreferredBhk() {
        return preferredBhk;
    }

    public void setPreferredBhk(Integer preferredBhk) {
        this.preferredBhk = preferredBhk;
    }

    public Integer getMinimumAreaSqft() {
        return minimumAreaSqft;
    }

    public void setMinimumAreaSqft(Integer minimumAreaSqft) {
        this.minimumAreaSqft = minimumAreaSqft;
    }

    public Integer getMinimumBathrooms() {
        return minimumBathrooms;
    }

    public void setMinimumBathrooms(Integer minimumBathrooms) {
        this.minimumBathrooms = minimumBathrooms;
    }

    public Integer getNumberOfOccupants() {
        return numberOfOccupants;
    }

    public void setNumberOfOccupants(Integer numberOfOccupants) {
        this.numberOfOccupants = numberOfOccupants;
    }

    public String getFurnishingPreference() {
        return furnishingPreference;
    }

    public void setFurnishingPreference(String furnishingPreference) {
        this.furnishingPreference = furnishingPreference;
    }

    public String getPreferredFloor() {
        return preferredFloor;
    }

    public void setPreferredFloor(String preferredFloor) {
        this.preferredFloor = preferredFloor;
    }

    public Integer getMinimumLeaseDuration() {
        return minimumLeaseDuration;
    }

    public void setMinimumLeaseDuration(Integer minimumLeaseDuration) {
        this.minimumLeaseDuration = minimumLeaseDuration;
    }

    public String getPreferredMoveInDate() {
        return preferredMoveInDate;
    }

    public void setPreferredMoveInDate(String preferredMoveInDate) {
        this.preferredMoveInDate = preferredMoveInDate;
    }
}
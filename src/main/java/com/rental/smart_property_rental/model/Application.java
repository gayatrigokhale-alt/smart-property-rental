package com.rental.smart_property_rental.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "APPLICATION")
public class Application {

    @Id
    private String id;

    @Field("Application_ID")
    private String applicationId;

    @Field("Tenant_ID")
    private String tenantId;

    @Field("Property_ID")
    private String propertyId;

    @Field("Application_Date")
    private String applicationDate;

    @Field("Proposed_Move_In_Date")
    private String proposedMoveInDate;

    @Field("Number_of_Occupants")
    private Integer numberOfOccupants;

    @Field("Application_Status")
    private String applicationStatus;

    public Application() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getProposedMoveInDate() {
        return proposedMoveInDate;
    }

    public void setProposedMoveInDate(String proposedMoveInDate) {
        this.proposedMoveInDate = proposedMoveInDate;
    }

    public Integer getNumberOfOccupants() {
        return numberOfOccupants;
    }

    public void setNumberOfOccupants(Integer numberOfOccupants) {
        this.numberOfOccupants = numberOfOccupants;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }
}
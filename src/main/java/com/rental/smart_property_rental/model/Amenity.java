package com.rental.smart_property_rental.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "AMENITY")
public class Amenity {

    @Id
    private String id;

    @Field("Amenity_ID")
    private String amenityId;

    @Field("Amenity_Name")
    private String amenityName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(String amenityId) {
        this.amenityId = amenityId;
    }

    public String getAmenityName() {
        return amenityName;
    }

    public void setAmenityName(String amenityName) {
        this.amenityName = amenityName;
    }
}
package com.rental.smart_property_rental.dto;

public class TenantLoginResponse {

    private String tenantId;
    private String firstName;
    private String lastName;
    private String email;

    public TenantLoginResponse() {
    }

    public TenantLoginResponse(String tenantId,
                               String firstName,
                               String lastName,
                               String email) {
        this.tenantId = tenantId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
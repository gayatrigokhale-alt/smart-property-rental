package com.rental.smart_property_rental.dto;

public class AdminLoginResponse {

    private String adminId;
    private String firstName;
    private String lastName;
    private String email;

    public AdminLoginResponse() {
    }

    public AdminLoginResponse(String adminId, String firstName,
                              String lastName, String email) {
        this.adminId = adminId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
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
package com.rental.smart_property_rental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UpdateTenantRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+91[- ]?)?[6-9]\\d{9}$",
            message = "Enter a valid Indian phone number"
    )
    private String phone;

    @NotBlank(message = "Gender is required")
    @Pattern(
            regexp = "^(Male|Female|Other)$",
            message = "Gender must be Male, Female, or Other"
    )
    private String gender;

    @NotBlank(message = "Occupation is required")
    private String occupation;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must not exceed 100")
    private Integer age;

    @NotBlank(message = "Tenant type is required")
    @Pattern(
            regexp = "^(Student|Working Professional|Family)$",
            message = "Tenant type must be Student, Working Professional, or Family"
    )
    private String tenantType;

    @NotNull(message = "Please specify whether you have pets")
    private Boolean hasPets;


    // Getters and setters

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTenantType() {
        return tenantType;
    }

    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }

    public Boolean getHasPets() {
        return hasPets;
    }

    public void setHasPets(Boolean hasPets) {
        this.hasPets = hasPets;
    }
}
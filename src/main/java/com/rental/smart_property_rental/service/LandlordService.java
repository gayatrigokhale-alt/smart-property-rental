
        package com.rental.smart_property_rental.service;
import com.rental.smart_property_rental.dto.LandlordUpdateRequest;
import com.rental.smart_property_rental.dto.LandlordRegisterRequest;
import com.rental.smart_property_rental.model.Landlord;
import com.rental.smart_property_rental.repository.LandlordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LandlordService {

    private final LandlordRepository landlordRepository;
    private final PasswordService passwordService;

    public LandlordService(
            LandlordRepository landlordRepository,
            PasswordService passwordService) {

        this.landlordRepository = landlordRepository;
        this.passwordService = passwordService;
    }

    public List<Landlord> getAllLandlords() {
        return landlordRepository.findAll();
    }

    // Landlord Registration
    public Landlord registerLandlord(LandlordRegisterRequest request) {

        // Check whether the email is already registered
        if (landlordRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        Landlord landlord = new Landlord();

        landlord.setFirstName(request.getFirstName());
        landlord.setLastName(request.getLastName());
        landlord.setEmail(request.getEmail());
        landlord.setPhone(request.getPhone());

        // Every newly registered landlord starts as Pending
        landlord.setVerificationStatus("Pending");

        // Hash the password before storing it
        String hashedPassword =
                passwordService.hashPassword(request.getPassword());

        landlord.setPasswordHash(hashedPassword);

        // Generate the next Landlord_ID
        int nextNumber = (int) landlordRepository.count() + 1;

        String landlordId =
                "L" + String.format("%03d", nextNumber);

        // Make sure the generated ID is actually unique
        while (landlordRepository.findByLandlordId(landlordId).isPresent()) {
            nextNumber++;

            landlordId =
                    "L" + String.format("%03d", nextNumber);
        }

        landlord.setLandlordId(landlordId);

        return landlordRepository.save(landlord);
    }
    // Landlord Login
    public Landlord loginLandlord(String email, String password) {

        Landlord landlord = landlordRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        boolean passwordCorrect =
                passwordService.verifyPassword(
                        password,
                        landlord.getPasswordHash()
                );

        if (!passwordCorrect) {
            throw new RuntimeException("Invalid email or password");
        }

        return landlord;
    }
    //Landlord Change password
    // Landlord Change Password
    public Landlord changePassword(
            String email,
            String currentPassword,
            String newPassword) {

        Landlord landlord = landlordRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Landlord not found"));

        boolean passwordCorrect =
                passwordService.verifyPassword(
                        currentPassword,
                        landlord.getPasswordHash()
                );

        if (!passwordCorrect) {
            throw new RuntimeException("Current password is incorrect");
        }

        String newHashedPassword =
                passwordService.hashPassword(newPassword);

        landlord.setPasswordHash(newHashedPassword);

        return landlordRepository.save(landlord);
    }
    //Update email
    public Landlord updateLandlord(
            String landlordId,
            LandlordUpdateRequest request) {

        Landlord landlord =
                landlordRepository.findByLandlordId(landlordId)
                        .orElseThrow(() ->
                                new RuntimeException("Landlord not found"));

        // Check whether the new email already belongs to another landlord
        landlordRepository.findByEmail(request.getEmail())
                .ifPresent(existingLandlord -> {

                    if (!existingLandlord.getLandlordId()
                            .equals(landlord.getLandlordId())) {

                        throw new RuntimeException(
                                "Email already registered"
                        );
                    }
                });

        landlord.setFirstName(request.getFirstName());
        landlord.setLastName(request.getLastName());
        landlord.setEmail(request.getEmail());
        landlord.setPhone(request.getPhone());

        return landlordRepository.save(landlord);
    }


}



package com.rental.smart_property_rental.service;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.repository.PropertyRepository;
import com.rental.smart_property_rental.repository.PropertyAmenityRepository;
import com.rental.smart_property_rental.repository.LeaseRepository;
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
    private final PropertyRepository propertyRepository;
    private final PropertyAmenityRepository propertyAmenityRepository;
    private final LeaseRepository leaseRepository;

    public LandlordService(
            LandlordRepository landlordRepository,
            PasswordService passwordService,
            PropertyRepository propertyRepository,
            PropertyAmenityRepository propertyAmenityRepository,
            LeaseRepository leaseRepository) {

        this.landlordRepository = landlordRepository;
        this.passwordService = passwordService;
        this.propertyRepository = propertyRepository;
        this.propertyAmenityRepository =
                propertyAmenityRepository;
        this.leaseRepository = leaseRepository;
    }

    public List<Landlord> getAllLandlords() {
        return landlordRepository.findAll();
    }
    public Landlord getLandlordById(String landlordId) {

        return landlordRepository.findByLandlordId(landlordId)
                .orElseThrow(() ->
                        new RuntimeException("Landlord not found"));
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
            String userId,
            LandlordUpdateRequest request) {

        if (userId == null || userId.isBlank()) {

            throw new RuntimeException(
                    "X-User-Id header is required"
            );
        }

        // A landlord can only update their own account
        if (!userId.equals(landlordId)) {

            throw new RuntimeException(
                    "You are not authorized to update this landlord profile"
            );
        }

        Landlord landlord =
                landlordRepository.findByLandlordId(landlordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Landlord not found"
                                ));

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
    // Delete landlord account
    public void deleteLandlord(
            String landlordId,
            String userId) {

        if (userId == null || userId.isBlank()) {

            throw new RuntimeException(
                    "X-User-Id header is required"
            );
        }

        // A landlord can only delete their own account
        if (!userId.equals(landlordId)) {

            throw new RuntimeException(
                    "You are not authorized to delete this landlord account"
            );
        }

        Landlord landlord =
                landlordRepository.findByLandlordId(landlordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Landlord not found"
                                ));

        // Get all properties belonging to this landlord
        List<Property> properties =
                propertyRepository.findByLandlordId(landlordId);

        // Check every property for an active lease
        for (Property property : properties) {

            boolean hasActiveLease =
                    leaseRepository
                            .findByPropertyId(
                                    property.getPropertyId()
                            )
                            .stream()
                            .anyMatch(lease ->
                                    "Active".equalsIgnoreCase(
                                            lease.getLeaseStatus()
                                    )
                            );

            if (hasActiveLease) {

                throw new RuntimeException(
                        "Landlord account cannot be deleted while an active lease exists for property "
                                + property.getPropertyId()
                );
            }
        }

        // No active leases exist.
        // Delete property amenities and properties.
        for (Property property : properties) {

            propertyAmenityRepository
                    .findByPropertyId(
                            property.getPropertyId()
                    )
                    .ifPresent(propertyAmenity ->
                            propertyAmenityRepository
                                    .delete(propertyAmenity)
                    );

            propertyRepository.delete(property);
        }

        // Finally delete landlord
        landlordRepository.delete(landlord);
    }

}


package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.exception.InvalidCredentialsException;
import com.rental.smart_property_rental.model.Admin;
import com.rental.smart_property_rental.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordService passwordService;

    public AdminService(AdminRepository adminRepository,
                        PasswordService passwordService) {
        this.adminRepository = adminRepository;
        this.passwordService = passwordService;
    }

    public Admin login(String email, String password) {

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        boolean passwordCorrect = passwordService.verifyPassword(
                password,
                admin.getPasswordHash()
        );

        if (!passwordCorrect) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return admin;
    }
}
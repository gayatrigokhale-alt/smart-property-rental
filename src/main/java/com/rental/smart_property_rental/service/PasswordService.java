package com.rental.smart_property_rental.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean verifyPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}
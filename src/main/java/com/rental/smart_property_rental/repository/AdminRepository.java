package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminRepository extends MongoRepository<Admin, String> {

    Optional<Admin> findByAdminId(String adminId);

    Optional<Admin> findByEmail(String email);
}
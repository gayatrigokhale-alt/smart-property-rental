package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.model.Amenity;
import com.rental.smart_property_rental.repository.AmenityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    private final AmenityRepository amenityRepository;

    public AmenityController(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @GetMapping
    public ResponseEntity<List<Amenity>> getAllAmenities() {
        return ResponseEntity.ok(amenityRepository.findAll());
    }
}
package com.rental.smart_property_rental.controller;


import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.repository.PropertyRepository;
import com.rental.smart_property_rental.service.PropertyMediaService;
import com.rental.smart_property_rental.service.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/properties")
public class PropertyMediaController {

    private final PropertyMediaService propertyMediaService;
    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;

    public PropertyMediaController(
            PropertyMediaService propertyMediaService,
            PropertyService propertyService,
            PropertyRepository propertyRepository) {

        this.propertyMediaService = propertyMediaService;
        this.propertyService = propertyService;
        this.propertyRepository = propertyRepository;
    }

    @PostMapping("/{propertyId}/images")
    public ResponseEntity<String> uploadImage(
            @PathVariable String propertyId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam("file") MultipartFile file) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                    .body("Login required.");
        }

        Property property = getOwnedProperty(propertyId, userId);

        String fileId =
                propertyMediaService.storeFile(file);


        property.getImageFileIds().add(fileId);

        propertyRepository.save(property);

        return ResponseEntity.ok(fileId);
    }
    @GetMapping("/media/{fileId}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String fileId) {

        GridFsResource resource =
                propertyMediaService.getFile(fileId);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                resource.getContentType()
                        )
                )
                .body(resource);
    }

    @PostMapping("/{propertyId}/images/multiple")
    public ResponseEntity<List<String>> uploadImages(
            @PathVariable String propertyId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam("files") List<MultipartFile> files) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        Property property = getOwnedProperty(propertyId, userId);

        List<String> fileIds = new ArrayList<>();

        for (MultipartFile file : files) {

            String fileId =
                    propertyMediaService.storeFile(file);

            fileIds.add(fileId);
        }

        property.getImageFileIds().addAll(fileIds);

        propertyRepository.save(property);

        return ResponseEntity.ok(fileIds);
    }

    @PostMapping("/{propertyId}/video")
    public ResponseEntity<String> uploadVideo(
            @PathVariable String propertyId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam("file") MultipartFile file) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        Property property = getOwnedProperty(propertyId, userId);

        String fileId =
                propertyMediaService.storeFile(file);

        property.setVideoFileId(fileId);

        propertyRepository.save(property);

        return ResponseEntity.ok(fileId);
    }
    private Property getOwnedProperty(String propertyId, String userId) {

        Property property = propertyRepository
                .findByPropertyId(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!userId.equals(property.getLandlordId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You can only upload media to your own properties."
            );
        }

        return property;
    }

    @DeleteMapping("/{propertyId}/images/{fileId}")
    public ResponseEntity<String> deleteImage(
            @PathVariable String propertyId,
            @PathVariable String fileId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body("Login required.");
        }

        Property property = getOwnedProperty(propertyId, userId);

        if (property.getImageFileIds() == null
                || !property.getImageFileIds().contains(fileId)) {
            return ResponseEntity.notFound().build();
        }

        property.getImageFileIds().remove(fileId);
        propertyRepository.save(property);

        boolean stillUsed = propertyRepository.findAll().stream()
                .anyMatch(p ->
                        (p.getImageFileIds() != null
                                && p.getImageFileIds().contains(fileId))
                                || fileId.equals(p.getVideoFileId()));

        if (!stillUsed) {
            propertyMediaService.deleteFile(fileId);
        }

        return ResponseEntity.ok("Photo removed successfully.");
    }

    @DeleteMapping("/{propertyId}/video")
    public ResponseEntity<String> deleteVideo(
            @PathVariable String propertyId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body("Login required.");
        }

        Property property = getOwnedProperty(propertyId, userId);
        String fileId = property.getVideoFileId();

        if (fileId == null || fileId.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        property.setVideoFileId(null);
        propertyRepository.save(property);

        boolean stillUsed = propertyRepository.findAll().stream()
                .anyMatch(p ->
                        (p.getImageFileIds() != null
                                && p.getImageFileIds().contains(fileId))
                                || fileId.equals(p.getVideoFileId()));

        if (!stillUsed) {
            propertyMediaService.deleteFile(fileId);
        }

        return ResponseEntity.ok("Video removed successfully.");
    }

}
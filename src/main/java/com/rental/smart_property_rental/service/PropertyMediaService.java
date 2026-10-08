package com.rental.smart_property_rental.service;

import org.springframework.data.mongodb.gridfs.GridFsResource;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.core.query.Query;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PropertyMediaService {

    private final GridFsTemplate gridFsTemplate;

    public PropertyMediaService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public String storeFile(MultipartFile file) {

        try {

            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );

            return fileId.toHexString();

        } catch (IOException e) {

            throw new RuntimeException("Failed to store file", e);
        }
    }
    public GridFsResource getFile(String fileId) {

        GridFSFile file =
                gridFsTemplate.findOne(
                        new Query(where("_id").is(new ObjectId(fileId)))
                );

        if (file == null) {
            throw new RuntimeException("File not found");
        }

        return gridFsTemplate.getResource(file);
    }
}
package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.Lease;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LeaseRepository extends MongoRepository<Lease, String> {

    Optional<Lease> findByLeaseId(String leaseId);

    boolean existsByLeaseId(String leaseId);

    Optional<Lease> findByApplicationId(String applicationId);

    List<Lease> findByTenantId(String tenantId);

    List<Lease> findByPropertyId(String propertyId);

}
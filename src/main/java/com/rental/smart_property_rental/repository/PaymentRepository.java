
package com.rental.smart_property_rental.repository;

import com.rental.smart_property_rental.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends MongoRepository<Payment, String> {

    Optional<Payment> findByPaymentId(String paymentId);

    boolean existsByPaymentId(String paymentId);

    List<Payment> findByApplicationId(String applicationId);

    Optional<Payment> findByApplicationIdAndPaymentStatus(
            String applicationId,
            String paymentStatus
    );

    Optional<Payment> findFirstByApplicationIdAndPaymentStatusOrderByPaymentIdAsc(
            String applicationId,
            String paymentStatus
    );

    List<Payment> findByTenantId(String tenantId);

    List<Payment> findByPropertyId(String propertyId);

    boolean existsByTransactionId(String transactionId);
}

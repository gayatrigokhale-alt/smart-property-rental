
package com.rental.smart_property_rental.service;

import com.rental.smart_property_rental.model.Payment;
import com.rental.smart_property_rental.model.Application;
import com.rental.smart_property_rental.model.Property;
import com.rental.smart_property_rental.repository.ApplicationRepository;
import com.rental.smart_property_rental.repository.LeaseRepository;
import com.rental.smart_property_rental.repository.PaymentRepository;
import com.rental.smart_property_rental.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final PropertyRepository propertyRepository;
    private final LeaseRepository leaseRepository;
    private final LeaseService leaseService;

    public PaymentService(
            PaymentRepository paymentRepository,
            ApplicationRepository applicationRepository,
            PropertyRepository propertyRepository,
            LeaseRepository leaseRepository,
            LeaseService leaseService) {

        this.paymentRepository = paymentRepository;
        this.applicationRepository = applicationRepository;
        this.propertyRepository = propertyRepository;
        this.leaseRepository = leaseRepository;
        this.leaseService = leaseService;
    }

    // INITIATE PAYMENT
    public Payment initiatePayment(
            String applicationId,
            String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            throw new RuntimeException("Tenant ID is required");
        }

        Application application = applicationRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        if (!tenantId.equals(application.getTenantId())) {
            throw new RuntimeException(
                    "You are not authorized to pay for this application");
        }

        if (!"Approved".equalsIgnoreCase(
                application.getApplicationStatus())) {
            throw new RuntimeException(
                    "Payment is allowed only for approved applications");
        }

        if (leaseRepository.findByApplicationId(applicationId).isPresent()) {
            throw new RuntimeException(
                    "A lease already exists for this application");
        }

        // Prevent another payment after successful payment.
        Payment successfulPayment = paymentRepository
                .findByApplicationIdAndPaymentStatus(
                        applicationId, "Successful")
                .orElse(null);

        if (successfulPayment != null) {
            throw new RuntimeException(
                    "Payment has already been completed for this application");
        }

        // Reuse an existing pending payment instead of creating a duplicate.
        Payment pendingPayment = paymentRepository
                .findFirstByApplicationIdAndPaymentStatusOrderByPaymentIdAsc(
                        applicationId, "Pending")
                .orElse(null);

        if (pendingPayment != null) {
            return pendingPayment;
        }

        Property property = propertyRepository
                .findByPropertyId(application.getPropertyId())
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!"Reserved".equalsIgnoreCase(property.getPropertyStatus())) {
            throw new RuntimeException(
                    "Property is not reserved for this application");
        }

        if (property.getMonthlyRent() == null
                || property.getSecurityDeposit() == null
                || property.getMonthlyRent() < 0
                || property.getSecurityDeposit() < 0) {
            throw new RuntimeException(
                    "Property rent or security deposit is invalid");
        }

        Payment payment = new Payment();

        payment.setPaymentId(generatePaymentId());
        payment.setApplicationId(application.getApplicationId());
        payment.setTenantId(application.getTenantId());
        payment.setPropertyId(application.getPropertyId());
        payment.setMonthlyRent(property.getMonthlyRent());
        payment.setSecurityDeposit(property.getSecurityDeposit());
        payment.setTotalAmount(
                property.getMonthlyRent() + property.getSecurityDeposit());
        payment.setPaymentMethod("Simulated");
        payment.setPaymentStatus("Pending");
        payment.setTransactionId(null);
        payment.setPaymentDate(null);

        return paymentRepository.save(payment);
    }

    // SIMULATE SUCCESSFUL PAYMENT
    public Payment simulateSuccessfulPayment(
            String paymentId,
            String tenantId) {

        Payment payment = paymentRepository
                .findByPaymentId(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        if (tenantId == null || tenantId.isBlank()
                || !tenantId.equals(payment.getTenantId())) {
            throw new RuntimeException(
                    "You are not authorized to complete this payment");
        }

        if ("Successful".equalsIgnoreCase(payment.getPaymentStatus())) {
            leaseService.createLeaseAfterSuccessfulPayment(
                    payment.getApplicationId(), tenantId);
            return payment;
        }

        if (!"Pending".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new RuntimeException(
                    "Only pending payments can be completed");
        }

        if (leaseRepository
                .findByApplicationId(payment.getApplicationId())
                .isPresent()) {
            throw new RuntimeException(
                    "A lease already exists for this application");
        }

        // Validate lease creation before recording payment success.
        leaseService.validateLeaseCanBeCreated(
                payment.getApplicationId(), tenantId);

        payment.setPaymentStatus("Successful");
        payment.setTransactionId("SIM-" + UUID.randomUUID());
        payment.setPaymentDate(LocalDateTime.now().toString());

        Payment savedPayment = paymentRepository.save(payment);

        leaseService.createLeaseAfterSuccessfulPayment(
                savedPayment.getApplicationId(), tenantId);

        return savedPayment;
    }

    // SIMULATE FAILED PAYMENT
    public Payment simulateFailedPayment(
            String paymentId,
            String tenantId) {

        Payment payment = paymentRepository
                .findByPaymentId(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        if (!payment.getTenantId().equals(tenantId)) {
            throw new RuntimeException(
                    "You are not authorized to update this payment");
        }

        if (!"Pending".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new RuntimeException(
                    "Only pending payments can fail");
        }

        payment.setPaymentStatus("Failed");
        payment.setTransactionId("SIM-" + UUID.randomUUID());
        payment.setPaymentDate(LocalDateTime.now().toString());

        return paymentRepository.save(payment);
    }

    private String generatePaymentId() {
        long count = paymentRepository.count() + 1;
        String paymentId = String.format("PAY%03d", count);

        while (paymentRepository.existsByPaymentId(paymentId)) {
            count++;
            paymentId = String.format("PAY%03d", count);
        }

        return paymentId;
    }

    // GET PAYMENTS BY TENANT
    public List<Payment> getPaymentsByTenant(String tenantId) {
        return paymentRepository.findByTenantId(tenantId);
    }
}

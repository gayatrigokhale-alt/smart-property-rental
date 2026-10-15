
        package com.rental.smart_property_rental.controller;

import com.rental.smart_property_rental.model.Payment;
import com.rental.smart_property_rental.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // GET PAYMENTS FOR A TENANT
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<?> getPaymentsByTenant(
            @PathVariable String tenantId,
            @RequestHeader(value = "X-User-Id", required = false)
            String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        if (!userId.equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to view these payments");
        }

        List<Payment> payments =
                paymentService.getPaymentsByTenant(tenantId);

        return ResponseEntity.ok(payments);
    }

    // INITIATE PAYMENT
    @PostMapping("/initiate/{applicationId}")
    public ResponseEntity<?> initiatePayment(
            @PathVariable String applicationId,
            @RequestHeader(value = "X-User-Id", required = false)
            String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {
            Payment payment = paymentService.initiatePayment(
                    applicationId, tenantId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(payment);

        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

    // SIMULATE SUCCESSFUL PAYMENT
    // DEMO ONLY — does not verify a real financial transaction.
    @PostMapping("/{paymentId}/simulate-success")
    public ResponseEntity<?> simulateSuccessfulPayment(
            @PathVariable String paymentId,
            @RequestHeader(value = "X-User-Id", required = false)
            String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {
            Payment payment =
                    paymentService.simulateSuccessfulPayment(
                            paymentId, tenantId);

            return ResponseEntity.ok(payment);

        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

    // SIMULATE FAILED PAYMENT
    // DEMO ONLY.
    @PostMapping("/{paymentId}/simulate-failure")
    public ResponseEntity<?> simulateFailedPayment(
            @PathVariable String paymentId,
            @RequestHeader(value = "X-User-Id", required = false)
            String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("X-User-Id header is required");
        }

        try {
            Payment payment =
                    paymentService.simulateFailedPayment(
                            paymentId, tenantId);

            return ResponseEntity.ok(payment);

        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

    // BASIC ERROR HANDLING
    private ResponseEntity<String> handleException(
            RuntimeException e) {

        String message = e.getMessage() == null
                ? "An unexpected error occurred"
                : e.getMessage();

        if (message.toLowerCase().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(message);
        }

        if (message.toLowerCase().contains("not authorized")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(message);
        }

        return ResponseEntity.badRequest().body(message);
    }
}

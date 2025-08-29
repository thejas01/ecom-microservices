package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.kafka.PaymentEventProducer;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public Payment createPayment(Payment payment) {
        log.info("Creating payment for order: {} amount: {} {}", payment.getOrderId(), payment.getAmount(), payment.getCurrency());

        // Check if payment already exists for this order
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(payment.getOrderId());
        if (existingPayment.isPresent() && 
            (existingPayment.get().getStatus() == PaymentStatus.COMPLETED ||
             existingPayment.get().getStatus() == PaymentStatus.PROCESSING)) {
            log.warn("Payment already exists for order: {}", payment.getOrderId());
            throw new IllegalStateException("Payment already exists for order: " + payment.getOrderId());
        }

        // Set initial status
        payment.setStatus(PaymentStatus.PENDING);
        Payment savedPayment = paymentRepository.save(payment);

        // Publish payment created event
        paymentEventProducer.sendPaymentCreatedEvent(savedPayment);

        return savedPayment;
    }

    @Transactional
    public Payment processPayment(String paymentId) {
        log.info("Processing payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING status: " + payment.getStatus());
        }

        try {
            // Update status to processing
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            // Process payment through gateway
            Map<String, Object> gatewayResponse = paymentGatewayService.processPayment(payment);
            
            boolean isSuccessful = (boolean) gatewayResponse.get("success");
            payment.setGatewayResponse(gatewayResponse.toString());

            if (isSuccessful) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setGatewayReference((String) gatewayResponse.get("gatewayReference"));
                payment.setProcessedAt(LocalDateTime.now());
                
                if (gatewayResponse.containsKey("cardLastFour")) {
                    payment.setCardLastFour((String) gatewayResponse.get("cardLastFour"));
                    payment.setCardBrand((String) gatewayResponse.get("cardBrand"));
                }

                // Publish payment completed event
                paymentEventProducer.sendPaymentCompletedEvent(payment);
                log.info("Payment completed successfully: {}", paymentId);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason((String) gatewayResponse.get("message"));
                payment.setFailedAt(LocalDateTime.now());

                // Publish payment failed event
                paymentEventProducer.sendPaymentFailedEvent(payment);
                log.error("Payment failed: {} reason: {}", paymentId, payment.getFailureReason());
            }

            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Error processing payment: {}", paymentId, e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("System error: " + e.getMessage());
            payment.setFailedAt(LocalDateTime.now());
            
            Payment savedPayment = paymentRepository.save(payment);
            paymentEventProducer.sendPaymentFailedEvent(savedPayment);
            
            throw new RuntimeException("Failed to process payment", e);
        }
    }

    @Transactional
    public Payment refundPayment(String paymentId, BigDecimal refundAmount) {
        log.info("Processing refund for payment: {} amount: {}", paymentId, refundAmount);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Can only refund completed payments");
        }

        if (refundAmount.compareTo(payment.getAmount().subtract(payment.getRefundAmount())) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds available amount");
        }

        try {
            Map<String, Object> refundResponse = paymentGatewayService.refundPayment(
                payment.getGatewayReference(), 
                refundAmount
            );

            boolean isSuccessful = (boolean) refundResponse.get("success");

            if (isSuccessful) {
                payment.setRefundAmount(payment.getRefundAmount().add(refundAmount));
                
                if (payment.getRefundAmount().equals(payment.getAmount())) {
                    payment.setStatus(PaymentStatus.REFUNDED);
                } else {
                    payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
                }

                Payment savedPayment = paymentRepository.save(payment);
                paymentEventProducer.sendPaymentRefundedEvent(savedPayment, refundAmount);
                
                log.info("Refund processed successfully for payment: {}", paymentId);
                return savedPayment;
            } else {
                throw new RuntimeException("Refund failed: " + refundResponse.get("message"));
            }

        } catch (Exception e) {
            log.error("Error processing refund for payment: {}", paymentId, e);
            throw new RuntimeException("Failed to process refund", e);
        }
    }

    @Transactional
    public Payment cancelPayment(String paymentId) {
        log.info("Cancelling payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending payments");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setFailedAt(LocalDateTime.now());
        payment.setFailureReason("Payment cancelled by user");

        Payment savedPayment = paymentRepository.save(payment);
        paymentEventProducer.sendPaymentCancelledEvent(savedPayment);

        return savedPayment;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByCustomerId(String customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Transactional
    public void expireOldPayments(int expiryMinutes) {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(expiryMinutes);
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(PaymentStatus.PENDING, expiryTime);

        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            payment.setFailedAt(LocalDateTime.now());
            payment.setFailureReason("Payment expired");
            
            Payment savedPayment = paymentRepository.save(payment);
            paymentEventProducer.sendPaymentExpiredEvent(savedPayment);
        }

        log.info("Expired {} payments", expiredPayments.size());
    }

    @Transactional(readOnly = true)
    public boolean isPaymentSuccessful(String orderId) {
        return paymentRepository.existsByOrderIdAndStatusIn(
            orderId, 
            List.of(PaymentStatus.COMPLETED, PaymentStatus.PROCESSING)
        );
    }
}
package com.b2b.paymentservice.app;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.B2B.events.payment.PaymentRequestedV1;
import com.B2B.extra.StatusPayment;
import com.B2B.topics.TopicNamesV1;
import com.b2b.paymentservice.api.dto.PaymentRequest;
import com.b2b.paymentservice.api.dto.PaymentResponse;
import com.b2b.paymentservice.domain.PaymentEntity;
import com.b2b.paymentservice.domain.PaymentRepository;
import com.b2b.paymentservice.handler.PaymentNotFoundException;
import com.b2b.paymentservice.outbox.OutboxEventEntity;
import com.b2b.paymentservice.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService
{
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);


    public PaymentServiceImpl(PaymentRepository paymentRepository, ObjectMapper objectMapper, OutboxEventRepository outboxEventRepository)
    {
        this.paymentRepository = paymentRepository;
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
    }


    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest)
    {
        PaymentEntity paymentEntity = new PaymentEntity(
                paymentRequest.senderAccountId(),
                paymentRequest.receiverAccountId(),
                paymentRequest.amount(),
                paymentRequest.currency(),
                StatusPayment.PENDING,
                Instant.now()
        );

        paymentRequest.reasonPayment().ifPresent(paymentEntity::setReasonPayment);

        paymentRepository.save(paymentEntity);

        PaymentRequestedV1 requestedEvent = new PaymentRequestedV1(
                UUID.randomUUID(),
                paymentEntity.getPaymentId(),
                paymentRequest.senderAccountId(),
                paymentRequest.receiverAccountId(),
                paymentRequest.amount(),
                paymentRequest.currency(),
                paymentEntity.getPaymentRequestedAt(),
                paymentRequest.reasonPayment().orElse(null)
        );

        String payloadJson;
        try
        {
            payloadJson = objectMapper.writeValueAsString(requestedEvent);
        }
        catch (JsonProcessingException e)
        {
            logger.error("Failed to serialize payment event: {}", e.getMessage());
            throw new RuntimeException("Outbox serialization failed", e);
        }


        OutboxEventEntity event = new OutboxEventEntity(
                TopicNamesV1.PAYMENT_REQUEST,
                "Payment.Requested",
                paymentEntity.getPaymentId().toString(),
                payloadJson
        );

        outboxEventRepository.save(event);

        return new PaymentResponse(paymentEntity.getPaymentId(),paymentEntity.getStatus(),paymentEntity.getPaymentRequestedAt(),paymentEntity.getAmount(),paymentEntity.getCurrency(),paymentEntity.getReasonPayment());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId)
    {
        PaymentEntity entity = paymentRepository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException("Payment not found with the provided id: " + paymentId));
        return new PaymentResponse(entity.getPaymentId(),entity.getStatus(),entity.getPaymentRequestedAt(),entity.getAmount(),entity.getCurrency(),entity.getReasonPayment());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments()
    {
        return paymentRepository.findAll().stream().map(response -> new PaymentResponse(response.getPaymentId(),response.getStatus(),response.getPaymentRequestedAt(),response.getAmount(),response.getCurrency(),response.getReasonPayment())).toList();
    }

    @Override
    @Transactional
    public void updatePayment(PaymentAcceptedV1 event)
    {
        paymentRepository.findById(event.getPaymentId()).ifPresent(paymentEntity -> {paymentEntity.updateStatus(StatusPayment.ACCEPTED);});
    }

    @Override
    @Transactional
    public void updatePayment(PaymentRejectedV1 event)
    {
        paymentRepository.findById(event.getPaymentId()).ifPresent(paymentEntity -> {paymentEntity.updateStatus(StatusPayment.REJECTED);});
    }

    @Override
    @Transactional
    public void updatePayment(UUID paymentId, StatusPayment status)
    {
        System.out.println(paymentRepository.findById(paymentId).orElse(null).getStatus().toString());
        paymentRepository.findById(paymentId).ifPresent(paymentEntity -> {
            paymentEntity.updateStatus(status);
        });
        System.out.println(paymentRepository.findById(paymentId).orElse(null).getStatus().toString());

    }


}

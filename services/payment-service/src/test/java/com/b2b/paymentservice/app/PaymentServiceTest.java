package com.b2b.paymentservice.app;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.B2B.extra.Currency;
import com.B2B.extra.RejectionCause;
import com.B2B.extra.StatusPayment;
import com.b2b.paymentservice.api.dto.PaymentRequest;
import com.b2b.paymentservice.api.dto.PaymentResponse;
import com.b2b.paymentservice.domain.PaymentEntity;
import com.b2b.paymentservice.domain.PaymentRepository;
import com.b2b.paymentservice.handler.PaymentNotFoundException;
import com.b2b.paymentservice.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest
{
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private ObjectMapper objectMapper;



    @InjectMocks
    private PaymentServiceImpl paymentService;


    @Test
    void createPayment_success() throws Exception
    {
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity entity = invocation.getArgument(0);
            java.lang.reflect.Field field = PaymentEntity.class.getDeclaredField("paymentId");
            field.setAccessible(true);
            field.set(entity, UUID.randomUUID());
            return entity;
        });

        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                Currency.EUR,
                Optional.empty()
        );

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        PaymentResponse result = paymentService.createPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(StatusPayment.PENDING);
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
        verify(outboxEventRepository, times(1)).save(any());
    }

    @Test
    void getPayment_success()
    {
        UUID paymentId = UUID.randomUUID();
        PaymentEntity entity = new PaymentEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(200),
                Currency.EUR,
                StatusPayment.PENDING,
                Instant.now()
        );

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(entity));

        PaymentResponse result = paymentService.getPayment(paymentId);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(StatusPayment.PENDING);
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void getPayment_notFound()
    {
        UUID paymentId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(paymentId))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void updatePayment_accepted()
    {
        Instant requestedDate = Instant.now();
        UUID paymentId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        PaymentAcceptedV1 request = new PaymentAcceptedV1(
                eventId,
                receiverId,
                paymentId,
                senderId,
                BigDecimal.valueOf(70),
                Currency.EUR,
                requestedDate,
                Instant.now(),
                null
        );
        PaymentEntity payment = new PaymentEntity(senderId,receiverId,BigDecimal.valueOf(70),Currency.EUR,StatusPayment.PENDING,requestedDate);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        paymentService.updatePayment(request);
        assertThat(payment.getStatus()).isEqualTo(StatusPayment.ACCEPTED);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void updatePayment_rejected()
    {
        Instant requestedDate = Instant.now();
        UUID paymentId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        PaymentRejectedV1 request = new PaymentRejectedV1(
                eventId,
                receiverId,
                paymentId,
                senderId,
                BigDecimal.valueOf(70),
                Currency.EUR,
                requestedDate,
                Instant.now(),
                null,
                RejectionCause.NOT_ENOUGH_FUNDS
        );
        PaymentEntity payment = new PaymentEntity(senderId,receiverId,BigDecimal.valueOf(100),Currency.EUR,StatusPayment.PENDING,requestedDate);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        paymentService.updatePayment(request);
        assertThat(payment.getStatus()).isEqualTo(StatusPayment.REJECTED);
        verify(paymentRepository, never()).save(any());
    }

}

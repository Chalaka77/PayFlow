package com.b2b.notificationservice.app;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.B2B.extra.Currency;
import com.B2B.extra.RejectionCause;
import com.b2b.notificationservice.domain.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest
{
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void sendNotificationAccepted()
    {
        PaymentAcceptedV1 event = new PaymentAcceptedV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(70),
                Currency.EUR,
                Instant.now(),
                Instant.now(),
                null
        );
        notificationService.sendNotificationAccepted(event);
        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    void sendNotificationRejected()
    {
        PaymentRejectedV1 event = new PaymentRejectedV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(70),
                Currency.EUR,
                Instant.now(),
                Instant.now(),
                null,
                RejectionCause.GENERAL_REJECTION
        );
        notificationService.sendNotificationRejected(event);
        verify(notificationRepository, times(1)).save(any());
    }
}

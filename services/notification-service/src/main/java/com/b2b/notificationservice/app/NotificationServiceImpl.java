package com.b2b.notificationservice.app;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.B2B.extra.StatusPayment;
import com.b2b.notificationservice.domain.NotificationEntity;
import com.b2b.notificationservice.domain.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService
{
    private final NotificationRepository notificationRepository;
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);


    public NotificationServiceImpl(NotificationRepository notificationRepository)
    {
        this.notificationRepository = notificationRepository;
    }


    @Override
    public void sendNotificationAccepted(PaymentAcceptedV1 event)
    {
        NotificationEntity notificationEntity = new NotificationEntity(event.getSenderAccountId(), "Payment Successfully sent", event.getAcceptedAt(), StatusPayment.ACCEPTED);
        notificationEntity.setPaymentId(event.getPaymentId());
        notificationRepository.save(notificationEntity);
    }

    @Override
    public void sendNotificationRejected(PaymentRejectedV1 event)
    {
        NotificationEntity notificationEntity = new NotificationEntity(event.getSenderAccountId(), "Payment Rejected", event.getRejectedAt(), StatusPayment.REJECTED);
        notificationRepository.save(notificationEntity);
    }
}

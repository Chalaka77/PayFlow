package com.b2b.notificationservice.app;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.b2b.notificationservice.domain.NotificationEntity;

public interface NotificationService
{
    void sendNotificationAccepted(PaymentAcceptedV1 event);
    void sendNotificationRejected(PaymentRejectedV1 event);
}

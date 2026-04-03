package com.b2b.notificationservice.handler;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.B2B.topics.TopicNamesV1;
import com.b2b.notificationservice.app.NotificationService;
import com.b2b.notificationservice.inbox.ProcessedEventEntity;
import com.b2b.notificationservice.inbox.ProcessedEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;


@Component
public class PaymentEventListener
{

    private final ProcessedEventRepository processedEventRepository;
    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);
    private final ObjectMapper objectMapper;

    public PaymentEventListener(ProcessedEventRepository processedEventRepository, NotificationService notificationService, ObjectMapper objectMapper)
    {
        this.processedEventRepository = processedEventRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(topics = TopicNamesV1.PAYMENT_ACCEPTED, groupId = "notification-service-group")
    public void handle(String rawMessage, Acknowledgment acknowledgment)
    {
        try
        {
            PaymentAcceptedV1 event = objectMapper.readValue(rawMessage, PaymentAcceptedV1.class);

            processedEventRepository.save(new ProcessedEventEntity(event.getEventId()));
            notificationService.sendNotificationAccepted(event);
            acknowledgment.acknowledge();
        }
        catch (DataIntegrityViolationException e)
        {
            logger.warn("Duplicate event skipped");
            acknowledgment.acknowledge();
            return;
        }
        catch (JsonProcessingException e)
        {
            logger.warn("Failed to deserialize event: {}", e.getMessage());
            acknowledgment.acknowledge();
            return;
        }
    }

    @KafkaListener(topics = TopicNamesV1.PAYMENT_REJECTED, groupId = "notification-service-group")
    public void handleRejected(String rawMessage, Acknowledgment acknowledgment)
    {
        try
        {
            PaymentRejectedV1 event = objectMapper.readValue(rawMessage, PaymentRejectedV1.class);

            processedEventRepository.save(new ProcessedEventEntity(event.getEventId()));
            notificationService.sendNotificationRejected(event);
            acknowledgment.acknowledge();
        }
        catch (DataIntegrityViolationException e)
        {
            logger.warn("Duplicate event skipped");
            acknowledgment.acknowledge();
            return;
        }
        catch (JsonProcessingException e)
        {
            logger.warn("Failed to deserialize event: {}", e.getMessage());
            acknowledgment.acknowledge();
            return;
        }
    }
}

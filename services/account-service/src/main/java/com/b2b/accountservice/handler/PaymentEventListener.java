package com.b2b.accountservice.handler;

import com.B2B.events.payment.PaymentRequestedV1;
import com.B2B.topics.TopicNamesV1;
import com.b2b.accountservice.app.AccountService;
import com.b2b.accountservice.inbox.ProcessedEventEntity;
import com.b2b.accountservice.inbox.ProcessedEventRepository;
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
    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);
    private final ObjectMapper objectMapper;

    public PaymentEventListener(ProcessedEventRepository processedEventRepository, AccountService accountService, ObjectMapper objectMapper)
    {
        this.processedEventRepository = processedEventRepository;
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(topics = TopicNamesV1.PAYMENT_REQUEST, groupId = "account-service-group")
    public void handle(String rawMessage, Acknowledgment acknowledgment)
    {
        try
        {
            PaymentRequestedV1 event = objectMapper.readValue(rawMessage, PaymentRequestedV1.class);

            processedEventRepository.save(new ProcessedEventEntity(event.getEventId()));
            accountService.processPayment(event);
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

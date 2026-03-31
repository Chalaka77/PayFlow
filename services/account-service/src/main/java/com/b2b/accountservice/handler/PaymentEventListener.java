package com.b2b.accountservice.handler;

import com.B2B.events.payment.PaymentRequestedV1;
import com.B2B.topics.TopicNamesV1;
import com.b2b.accountservice.app.AccountService;
import com.b2b.accountservice.inbox.ProcessedEventEntity;
import com.b2b.accountservice.inbox.ProcessedEventRepository;
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

    public PaymentEventListener(ProcessedEventRepository processedEventRepository, AccountService accountService)
    {
        this.processedEventRepository = processedEventRepository;
        this.accountService = accountService;
    }


    @KafkaListener(topics = TopicNamesV1.PAYMENT_REQUEST, groupId = "account-service-group")
    public void handle(PaymentRequestedV1 event, Acknowledgment acknowledgment)
    {
        try
        {
            processedEventRepository.save(new ProcessedEventEntity(event.getEventId()));
        }
        catch (DataIntegrityViolationException e)
        {
            logger.warn("Duplicate event skipped: {}", event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        accountService.processPayment(event);
        acknowledgment.acknowledge();
    }
}

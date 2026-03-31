package com.b2b.accountservice.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxEventPublisher
{
    private static final Logger logger = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;

    @Value("${outbox.scheduler.batch-size}")
    private int batchSize;

    public OutboxEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                OutboxEventRepository outboxEventRepository)
    {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${outbox.scheduler.delay-ms}")
    public void publishPendingEvents()
    {
        List<OutboxEventEntity> events = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc("NEW", PageRequest.of(0, batchSize));

        for (OutboxEventEntity event : events)
        {
            try
            {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayloadJson());
                event.markAsSent();
            }
            catch (Exception e)
            {
                logger.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }
}
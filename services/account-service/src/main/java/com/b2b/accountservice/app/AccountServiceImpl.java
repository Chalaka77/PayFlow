package com.b2b.accountservice.app;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.B2B.events.payment.PaymentRequestedV1;
import com.B2B.extra.AccountStatus;
import com.B2B.extra.RejectionCause;
import com.B2B.topics.TopicNamesV1;
import com.b2b.accountservice.domain.AccountBalanceEntity;
import com.b2b.accountservice.domain.AccountEntity;
import com.b2b.accountservice.domain.AccountRepository;
import com.b2b.accountservice.exceptions.AccountNotActiveException;
import com.b2b.accountservice.exceptions.AccountNotFoundException;
import com.b2b.accountservice.exceptions.CurrencyNotSupportedException;
import com.b2b.accountservice.exceptions.InsufficientFundsException;
import com.b2b.accountservice.outbox.OutboxEventEntity;
import com.b2b.accountservice.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Service
public class AccountServiceImpl implements AccountService
{
    private final AccountRepository accountRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    public AccountServiceImpl(AccountRepository accountRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper)
    {
        this.accountRepository = accountRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

//    TODO verificare e aggiungere sad path e pubblicare evento rigettato.
    @Override
    @Transactional
    public void processPayment(PaymentRequestedV1 requestEvent)
    {
        try
        {
//        1. Check sender and receiver ID, or AccountNotFoundException
            if (requestEvent.getSenderAccountId() == null || requestEvent.getReceiverAccountId() == null)
                throw new IllegalArgumentException("Sender or Receiver account ID is null");
            if (requestEvent.getSenderAccountId().equals(requestEvent.getReceiverAccountId()))
                throw new IllegalArgumentException("Sender or Receiver account ID is the same");

            AccountEntity sender = accountRepository.findById(requestEvent.getSenderAccountId()).orElseThrow(() -> new AccountNotFoundException("Sender account ID does not exist"));
            AccountEntity receiver = accountRepository.findById(requestEvent.getReceiverAccountId()).orElseThrow(() -> new AccountNotFoundException("Receiver account ID does not exist"));

//        2. Check sender/receiver are active, or AccountNotActiveException
            if (sender.getAccountStatus() != AccountStatus.ACTIVE)
                throw new AccountNotActiveException("Sender account status is not ACTIVE");
            if (receiver.getAccountStatus() != AccountStatus.ACTIVE)
                throw new AccountNotActiveException("Receiver account status is not ACTIVE");

//        3. Verify sender has the currency selected, or CurrencyNotSupportedException
            AccountBalanceEntity senderBalance = sender.getSaldi().stream().filter(b -> b.getCurrency().equals(requestEvent.getCurrency())).findFirst().orElseThrow(() -> new CurrencyNotSupportedException(requestEvent.getCurrency().name()));

//        4. Check the sender has >= amount requested, or InsufficientFundsException
            if (senderBalance.getBalance().compareTo(requestEvent.getAmount()) < 0)
                throw new InsufficientFundsException(requestEvent.getSenderAccountId().toString());

//        5. Verify receiver has the currency chosen, or CurrencyNotSupportedException
            AccountBalanceEntity receiverBalance = receiver.getSaldi().stream()
                    .filter(b -> b.getCurrency().equals(requestEvent.getCurrency()))
                    .findFirst()
                    .orElseThrow(() -> new CurrencyNotSupportedException(requestEvent.getCurrency().name()));

//        6. Update saldo sender (-) e receiver (+)
            senderBalance.updateBalance(senderBalance.getBalance().subtract(requestEvent.getAmount()));
            receiverBalance.updateBalance(receiverBalance.getBalance().add(requestEvent.getAmount()));

            PaymentAcceptedV1 acceptedEvent = new PaymentAcceptedV1(
                    UUID.randomUUID(),
                    requestEvent.getReceiverAccountId(),
                    requestEvent.getPaymentId(),
                    requestEvent.getSenderAccountId(),
                    requestEvent.getAmount(),
                    requestEvent.getCurrency(),
                    requestEvent.getRequestedAt(),
                    Instant.now(),
                    requestEvent.getPaymentCause()
            );

            String payloadJson;
            try
            {
                payloadJson = objectMapper.writeValueAsString(acceptedEvent);
            }
            catch (JsonProcessingException e)
            {
                logger.error("Failed to serialize payment event: {}", e.getMessage());
                throw new RuntimeException("Outbox serialization failed", e);
            }

            OutboxEventEntity event = new OutboxEventEntity(
                    TopicNamesV1.PAYMENT_ACCEPTED,
                    "Payment.Accepted",
                    requestEvent.getPaymentId().toString(),
                    payloadJson
            );
//            7. Publish event accepted/rejected

            outboxEventRepository.save(event);
        } catch (AccountNotFoundException | AccountNotActiveException | CurrencyNotSupportedException | InsufficientFundsException e)
        {
            RejectionCause rejectionCause = switch (e)
            {
                case AccountNotActiveException ex -> RejectionCause.USER_NOT_ACTIVE;
                case AccountNotFoundException ex -> RejectionCause.USER_NOT_FOUND;
                case CurrencyNotSupportedException ex -> RejectionCause.CURRENCY_NOT_SUPPORTED;
                case InsufficientFundsException ex -> RejectionCause.NOT_ENOUGH_FUNDS;
                default -> RejectionCause.GENERAL_REJECTION;
            };

            PaymentRejectedV1 rejectEvent = new PaymentRejectedV1(
                    UUID.randomUUID(),
                    requestEvent.getReceiverAccountId(),
                    requestEvent.getSenderAccountId(),
                    requestEvent.getAmount(),
                    requestEvent.getCurrency(),
                    requestEvent.getRequestedAt(),
                    Instant.now(),
                    requestEvent.getPaymentCause(),
                    rejectionCause
            );

            try
            {
                String rejectPayload = objectMapper.writeValueAsString(rejectEvent);
                outboxEventRepository.save(new OutboxEventEntity(
                        TopicNamesV1.PAYMENT_REJECTED,
                        "Payment.Rejected",
                        requestEvent.getPaymentId().toString(),
                        rejectPayload
                ));
            }
            catch (JsonProcessingException e1)
            {
                logger.error("Failed to serialize payment event: {}", e1.getMessage());
                throw new RuntimeException("Outbox serialization failed", e1);
            }

        }

    }
}

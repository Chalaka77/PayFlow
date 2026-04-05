package com.b2b.accountservice.app;

import com.B2B.events.payment.PaymentRequestedV1;
import com.B2B.extra.AccountStatus;
import com.B2B.extra.Currency;
import com.b2b.accountservice.domain.AccountBalanceEntity;
import com.b2b.accountservice.domain.AccountEntity;
import com.b2b.accountservice.domain.AccountRepository;
import com.b2b.accountservice.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest
{

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AccountServiceImpl accountService;


    @Test
    void processPayment_accepted() throws Exception
    {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AccountEntity entitySender = new AccountEntity(
                "senderEmail@mail.com",
                new ArrayList<>());

        AccountBalanceEntity balanceSender = new AccountBalanceEntity(Currency.EUR, entitySender);
        balanceSender.updateBalance(new BigDecimal("1000"));
        entitySender.getSaldi().add(balanceSender);


        AccountEntity entityReceiver = new AccountEntity(
                "receiverEmail@mail.com",
                new ArrayList<>());

        AccountBalanceEntity balanceReceiver = new AccountBalanceEntity(Currency.EUR, entityReceiver);
        balanceReceiver.updateBalance(new BigDecimal("1000"));
        entityReceiver.getSaldi().add(balanceReceiver);
        PaymentRequestedV1 paymentRequest = new PaymentRequestedV1(
                eventId,
                paymentId,
                senderId,
                receiverId,
                BigDecimal.valueOf(100),
                Currency.EUR,
                Instant.now(),
                null
        );

        when(accountRepository.findById(senderId)).thenReturn(Optional.of(entitySender));
        when(accountRepository.findById(receiverId)).thenReturn(Optional.of(entityReceiver));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        accountService.processPayment(paymentRequest);
        assertThat(balanceSender.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(900));
        assertThat(balanceReceiver.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1100));
        verify(outboxEventRepository, times(1)).save(any());
    }

    @Test
    void processPayment_senderNotFound() throws Exception
    {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        PaymentRequestedV1 paymentRequest = new PaymentRequestedV1(
                eventId,
                paymentId,
                senderId,
                receiverId,
                BigDecimal.valueOf(100),
                Currency.EUR,
                Instant.now(),
                null
        );

        when(accountRepository.findById(senderId)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        accountService.processPayment(paymentRequest);
        verify(outboxEventRepository, times(1)).save(any());
    }

    @Test
    void processPayment_senderNotActive() throws Exception
    {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AccountEntity entitySender = new AccountEntity(
                "senderEmail@mail.com",
                new ArrayList<>());
        entitySender.changeAccountStatus(AccountStatus.DISABLED);
        AccountEntity entityReceiver = new AccountEntity(
                "receiverEmail@mail.com",
                new ArrayList<>());
        PaymentRequestedV1 paymentRequest = new PaymentRequestedV1(
                eventId,
                paymentId,
                senderId,
                receiverId,
                BigDecimal.valueOf(100),
                Currency.EUR,
                Instant.now(),
                null
        );
        when(accountRepository.findById(senderId)).thenReturn(Optional.of(entitySender));
        when(accountRepository.findById(receiverId)).thenReturn(Optional.of(entityReceiver));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        accountService.processPayment(paymentRequest);
        verify(outboxEventRepository, times(1)).save(any());
    }

    @Test
    void processPayment_currencyNotSupported() throws Exception
    {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AccountEntity entitySender = new AccountEntity(
                "senderEmail@mail.com",
                new ArrayList<>());

        AccountBalanceEntity balanceSender = new AccountBalanceEntity(Currency.USD, entitySender);
        balanceSender.updateBalance(new BigDecimal("1000"));
        entitySender.getSaldi().add(balanceSender);
        AccountEntity entityReceiver = new AccountEntity(
                "receiverEmail@mail.com",
                new ArrayList<>());

        AccountBalanceEntity balanceReceiver = new AccountBalanceEntity(Currency.EUR, entityReceiver);
        balanceReceiver.updateBalance(new BigDecimal("1000"));
        entityReceiver.getSaldi().add(balanceReceiver);
        PaymentRequestedV1 paymentRequest = new PaymentRequestedV1(
                eventId,
                paymentId,
                senderId,
                receiverId,
                BigDecimal.valueOf(100),
                Currency.EUR,
                Instant.now(),
                null
        );
        when(accountRepository.findById(senderId)).thenReturn(Optional.of(entitySender));
        when(accountRepository.findById(receiverId)).thenReturn(Optional.of(entityReceiver));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        accountService.processPayment(paymentRequest);
        verify(outboxEventRepository, times(1)).save(any());
    }

    @Test
    void processPayment_insufficientFunds() throws Exception
    {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AccountEntity entitySender = new AccountEntity(
                "senderEmail@mail.com",
                new ArrayList<>());

        AccountBalanceEntity balanceSender = new AccountBalanceEntity(Currency.EUR, entitySender);
        balanceSender.updateBalance(new BigDecimal("50"));
        entitySender.getSaldi().add(balanceSender);

        AccountEntity entityReceiver = new AccountEntity(
                "receiverEmail@mail.com",
                new ArrayList<>());

        AccountBalanceEntity balanceReceiver = new AccountBalanceEntity(Currency.EUR, entityReceiver);
        balanceReceiver.updateBalance(new BigDecimal("1000"));
        entityReceiver.getSaldi().add(balanceReceiver);
        PaymentRequestedV1 paymentRequest = new PaymentRequestedV1(
                eventId,
                paymentId,
                senderId,
                receiverId,
                BigDecimal.valueOf(100),
                Currency.EUR,
                Instant.now(),
                null
        );
        when(accountRepository.findById(senderId)).thenReturn(Optional.of(entitySender));
        when(accountRepository.findById(receiverId)).thenReturn(Optional.of(entityReceiver));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        accountService.processPayment(paymentRequest);
        verify(outboxEventRepository, times(1)).save(any());
    }



}

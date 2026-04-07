package com.b2b.accountservice.app;

import com.B2B.events.payment.PaymentRequestedV1;
import com.B2B.extra.AccountStatus;
import com.B2B.extra.Currency;
import com.b2b.accountservice.api.dto.AddBalanceRequest;
import com.b2b.accountservice.api.dto.CreateAccountRequest;
import com.b2b.accountservice.api.dto.CreateAccountResponse;
import com.b2b.accountservice.api.dto.UpdateAccountStatusRequest;
import com.b2b.accountservice.domain.AccountBalanceEntity;
import com.b2b.accountservice.domain.AccountBalanceRepository;
import com.b2b.accountservice.domain.AccountEntity;
import com.b2b.accountservice.domain.AccountRepository;
import com.b2b.accountservice.exceptions.AccountNotFoundException;
import com.b2b.accountservice.exceptions.CurrencyAlreadyExists;
import com.b2b.accountservice.exceptions.CurrencyNotSupportedException;
import com.b2b.accountservice.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Mock
    private AccountBalanceRepository accountBalanceRepository;

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

    // createAccount
    @Test
    void createAccount_success()
    {
        CreateAccountRequest request = new CreateAccountRequest("test@yahoo.com");

        accountService.createAccount(request);
        verify(accountRepository, times(1)).save(any());
    }
    @Test
    void createAccount_emailAlreadyInUse()
    {
        CreateAccountRequest request = new CreateAccountRequest("email@yahoo.com");

        when(accountRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> accountService.createAccount(request));
    }

    // changeAccountStatus
    @Test
    void changeAccountStatus_success()
    {
        UUID accountId = UUID.randomUUID();
        AccountEntity account = new AccountEntity(
                "account@mail.com",
                new ArrayList<>());

        UpdateAccountStatusRequest updateAccountStatusRequest = new UpdateAccountStatusRequest(AccountStatus.DISABLED);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        accountService.changeAccountStatus(accountId, updateAccountStatusRequest);
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.DISABLED);

    }

    @Test
    void changeAccountStatus_accountNotFound()
    {
        UUID accountId = UUID.randomUUID();
        UpdateAccountStatusRequest updateAccountStatusRequest = new UpdateAccountStatusRequest(AccountStatus.DISABLED);

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.changeAccountStatus(accountId, updateAccountStatusRequest));
    }

    // addBalance
    @Test
    void addBalance_success()
    {
        UUID accountId = UUID.randomUUID();
        AccountEntity account = new AccountEntity("accouhnt@mail.com", new ArrayList<>());
        AddBalanceRequest request =  new AddBalanceRequest(Currency.EUR,BigDecimal.valueOf(100));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        accountService.addBalanceOnCurrency(request,accountId);
        assertThat(account.getSaldi()).size().isEqualTo(1);
        assertThat(account.getSaldi().getFirst().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void addBalance_accountNotFound()
    {
        UUID accountId = UUID.randomUUID();
        AddBalanceRequest request = new AddBalanceRequest(Currency.EUR,BigDecimal.valueOf(100));

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.addBalanceOnCurrency(request,accountId));
    }

    @Test
    void addBalance_currencyAlreadyExists()
    {
        UUID accountId = UUID.randomUUID();
        AccountEntity account = new AccountEntity("account@mail.com", new ArrayList<>());
        AccountBalanceEntity balance = new AccountBalanceEntity(Currency.EUR, account);
        balance.updateBalance(BigDecimal.valueOf(100));
        account.getSaldi().add(balance);
        AddBalanceRequest request =  new AddBalanceRequest(Currency.EUR,BigDecimal.valueOf(100));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        assertThrows(CurrencyAlreadyExists.class, () -> accountService.addBalanceOnCurrency(request,accountId));
    }

    // updateBalance
    @Test
    void updateBalance_success()
    {
        UUID accountId = UUID.randomUUID();
        AccountEntity account = new AccountEntity("account@mail.com", new ArrayList<>());
        AccountBalanceEntity balance = new AccountBalanceEntity(Currency.EUR, account);
        balance.updateBalance(BigDecimal.valueOf(100));
        account.getSaldi().add(balance);
        Currency currencyToUpdate = Currency.EUR;
        BigDecimal balanceToUpdate = BigDecimal.valueOf(77);
        BigDecimal sumExpected = balance.getBalance().add(balanceToUpdate);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        accountService.updateBalance(accountId,currencyToUpdate,balanceToUpdate);
        assertThat(account.getSaldi().getFirst().getBalance()).isEqualByComparingTo(sumExpected);
    }

    @Test
    void updateBalance_accountNotFound()
    {
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.updateBalance(accountId,Currency.EUR,BigDecimal.valueOf(100)));
    }

    @Test
    void updateBalance_currencyNotSupported()
    {
        UUID accountId = UUID.randomUUID();
        AccountEntity account = new AccountEntity("account@mail.com", new ArrayList<>());
        AccountBalanceEntity balance = new AccountBalanceEntity(Currency.EUR, account);
        balance.updateBalance(BigDecimal.valueOf(100));
        account.getSaldi().add(balance);
        Currency currencyToUpdate = Currency.USD;
        BigDecimal balanceToUpdate = BigDecimal.valueOf(77);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        assertThrows(CurrencyNotSupportedException.class , () -> accountService.updateBalance(accountId,currencyToUpdate,balanceToUpdate));
    }


}

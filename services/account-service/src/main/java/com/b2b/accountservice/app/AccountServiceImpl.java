package com.b2b.accountservice.app;

import com.B2B.events.account.PaymentAcceptedV1;
import com.B2B.events.account.PaymentRejectedV1;
import com.B2B.events.payment.PaymentRequestedV1;
import com.B2B.extra.AccountStatus;
import com.B2B.extra.Currency;
import com.B2B.extra.RejectionCause;
import com.B2B.topics.TopicNamesV1;
import com.b2b.accountservice.api.dto.*;
import com.b2b.accountservice.domain.AccountBalanceEntity;
import com.b2b.accountservice.domain.AccountBalanceRepository;
import com.b2b.accountservice.domain.AccountEntity;
import com.b2b.accountservice.domain.AccountRepository;
import com.b2b.accountservice.exceptions.*;
import com.b2b.accountservice.outbox.OutboxEventEntity;
import com.b2b.accountservice.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;


@Service
public class AccountServiceImpl implements AccountService
{
    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    public AccountServiceImpl(AccountRepository accountRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper,  AccountBalanceRepository accountBalanceRepository)
    {
        this.accountRepository = accountRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.accountBalanceRepository = accountBalanceRepository;
    }

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
                    requestEvent.getPaymentId(),
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

/* TODO fix da fare:
    GetAccount ID :  se id non presente va in 500.

    UPDATE STATUS : non aggiorna, se id presente va in 500.

    aggiorna BALANCE, sostituisce i valori presenti al posto di aggiornare. , se valuta non presente va in 500.

    create account, non blocca creazione se email gia' presente, crea altri account con stessa email. Non c'e' controllo su formato email. Se inserisco un numero, lo accetta e crea account.

    add new Balanace, se gia' presente va in 500. anche qua se account non presente va in 500.

    findBy email : se si mette email non presente, va in 500.
 */



    @Override
    @Transactional(readOnly = true)
    public AccountResponse findAccountByEmail(String email)
    {
        AccountEntity entity = accountRepository.findByEmail(email).orElseThrow(() -> new AccountNotFoundException("No account found with the email: " + email));
        return new AccountResponse(entity.getAccountId(),entity.getEmail(),entity.getAccountStatus(),entity.getSaldi().stream().map(balanceSaldo -> new AccountBalanceResponse(balanceSaldo.getCurrency(), balanceSaldo.getBalance())).toList());
    }

    @Override
    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest request)
    {
        AccountEntity entity = new AccountEntity(request.email());
        accountRepository.save(entity);
        return new CreateAccountResponse(entity.getEmail(), entity.getAccountStatus());
    }

    @Override
    @Transactional
    public AddBalanceResponse addBalanceOnCurrency(AddBalanceRequest request, UUID accountId)
    {
        AccountEntity entity = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId.toString()));
        AccountBalanceEntity balanceToAdd;
        List<AccountBalanceEntity> accountBalanceEntityList = entity.getSaldi();

        boolean currencyExist = accountBalanceEntityList.stream().anyMatch(balance -> balance.getCurrency().equals(request.currency()));

        if(currencyExist)
            throw new CurrencyAlreadyExists("Currency: "+ request.currency()+  " already exists");
        else
        {
            balanceToAdd = new AccountBalanceEntity(request.currency(), entity);
            accountBalanceEntityList.add(balanceToAdd);
            balanceToAdd.updateBalance(request.amount());
            accountBalanceRepository.save(balanceToAdd);
            return new AddBalanceResponse(balanceToAdd.getCurrency(),balanceToAdd.getBalance());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountDetail(UUID accountId)
    {
        AccountEntity entity = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId.toString()));
        List<AccountBalanceResponse> listAccountBalance = entity.getSaldi().stream().map(balanceSaldo -> new AccountBalanceResponse(balanceSaldo.getCurrency(), balanceSaldo.getBalance())).toList();

        return new AccountResponse(entity.getAccountId(), entity.getEmail(), entity.getAccountStatus(), listAccountBalance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> findAllAccounts()
    {
        List<AccountEntity> accountEntities = accountRepository.findAll();

        return accountEntities.stream().map(entity -> new AccountResponse(entity.getAccountId(), entity.getEmail(), entity.getAccountStatus(), entity.getSaldi().stream().map(balanceSaldo -> new AccountBalanceResponse(balanceSaldo.getCurrency(), balanceSaldo.getBalance())).toList())).toList();
    }

    @Override
    @Transactional
    public void updateBalance(UUID accountId, Currency currency, BigDecimal amount)
    {
        AccountEntity entity = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId.toString()));
        AccountBalanceEntity balanceToUpdate = entity.getSaldi().stream().filter(balance -> balance.getCurrency().equals(currency)).findFirst().orElseThrow(() -> new CurrencyNotSupportedException(currency.toString()));
        balanceToUpdate.updateBalance(amount);
    }

    @Override
    @Transactional
    public void changeAccountStatus(UUID accountId, UpdateAccountStatusRequest status)
    {
        AccountEntity entity = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId.toString()));
        entity.changeAccountStatus(status.accountStatus());
    }


}

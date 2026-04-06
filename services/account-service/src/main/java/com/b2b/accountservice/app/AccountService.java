package com.b2b.accountservice.app;

import com.B2B.events.payment.PaymentRequestedV1;
import com.B2B.extra.AccountStatus;
import com.B2B.extra.Currency;
import com.b2b.accountservice.api.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService
{
// checks on account
    AccountResponse findAccountByEmail(String email);

    // Account actions
    CreateAccountResponse createAccount(CreateAccountRequest request);
    AddBalanceResponse addBalanceOnCurrency(AddBalanceRequest request, UUID accountId);
    AccountResponse getAccountDetail(UUID accountId);
    List<AccountResponse> findAllAccounts();
    void updateBalance(UUID accountId, Currency currency, BigDecimal amount);

// change AccountStatus
    void changeAccountStatus(UUID accountId, UpdateAccountStatusRequest status);

//  Process payment
    void processPayment(PaymentRequestedV1 requestEvent);

}

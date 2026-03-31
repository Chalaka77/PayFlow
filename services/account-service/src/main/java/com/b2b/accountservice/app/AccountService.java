package com.b2b.accountservice.app;

import com.B2B.events.payment.PaymentRequestedV1;
import com.b2b.accountservice.domain.AccountEntity;

import java.util.List;

public interface AccountService
{
//// API Methods to add once REST layer implemented.
//// checks on account
//    boolean existsByAccountId(UUID accountId);
//    AccountStatus getAccountStatus(UUID accountId);
//    AccountEntity findAccountByEmail(String email);
//
//// change AccountStatus
//    void disableAccount(UUID accountId);
//    void enableAccount(UUID accountId);
//    void blockAccount(UUID accountId);
//    List<AccountEntity> getAllAccounts();

//  Process payment
    void processPayment(PaymentRequestedV1 requestEvent);

}

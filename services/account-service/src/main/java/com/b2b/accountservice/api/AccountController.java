package com.b2b.accountservice.api;

import com.B2B.extra.Currency;
import com.b2b.accountservice.api.dto.*;
import com.b2b.accountservice.app.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/account")
public class AccountController
{
    private final AccountService accountService;

    public AccountController(AccountService accountService)
    {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest)
    {
        return new ResponseEntity<>(accountService.createAccount(createAccountRequest), HttpStatus.CREATED);
    }

    @PostMapping("/{accountId}/balance")
    @Operation(summary = "Add a new Currency and Balance")
    public ResponseEntity<AddBalanceResponse> addBalance(@Valid @RequestBody AddBalanceRequest addBalanceRequest,@Valid @PathVariable UUID accountId)
    {
        return new ResponseEntity<>(accountService.addBalanceOnCurrency(addBalanceRequest,accountId), HttpStatus.CREATED);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Find account by email")
    public ResponseEntity<AccountResponse> getAccountByEmail(@Valid @PathVariable String email)
    {
        return new ResponseEntity<>(accountService.findAccountByEmail(email), HttpStatus.OK);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get Account")
    public ResponseEntity<AccountResponse> getAccount(@Valid @PathVariable UUID accountId)
    {
        return new ResponseEntity<>(accountService.getAccountDetail(accountId), HttpStatus.OK);
    }

    @GetMapping("/all")
    @Operation(summary = "Get List of all accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts()
    {
        return new ResponseEntity<>(accountService.findAllAccounts(), HttpStatus.OK);
    }

    @PutMapping("/{accountId}/balance/{currency}")
    @Operation(summary = "Update Balance of single conto (Currency/Amount)")
    public ResponseEntity<Void> updateAccountBalance(@Valid @PathVariable UUID accountId,@Valid @PathVariable Currency currency,@Valid @RequestBody BigDecimal amount)
    {
        accountService.updateBalance(accountId,currency,amount);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Change Status of an Account")
    public ResponseEntity<Void> updateAccountStatus(@PathVariable UUID accountId, @RequestBody UpdateAccountStatusRequest status)
    {
        accountService.changeAccountStatus(accountId,status);
        return ResponseEntity.noContent().build();
    }











}

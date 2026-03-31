package com.b2b.accountservice.domain;

import com.B2B.extra.Currency;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "account_balance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"account_id", "currency"})
})
public class AccountBalanceEntity
{
    @Id
    @UuidGenerator
    private UUID balanceId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;


    public AccountBalanceEntity( Currency currency, AccountEntity account)
    {
        this.currency = currency;
        this.balance = BigDecimal.valueOf(0);
        this.account = account;
    }

    protected AccountBalanceEntity(){}

    public void updateBalance(BigDecimal balance)
    {
        this.balance = balance;
    }


    public UUID getBalanceId()
    {
        return balanceId;
    }

    public Currency getCurrency()
    {
        return currency;
    }

    public BigDecimal getBalance()
    {
        return balance;
    }

    public AccountEntity getAccount()
    {
        return account;
    }
}

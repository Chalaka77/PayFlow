package com.b2b.accountservice.domain;

import com.B2B.extra.AccountStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "account")
public class AccountEntity
{

    @Id
    @UuidGenerator
    private UUID accountId;

    @Column(length = 50, nullable = false, unique = true)
    private String email;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AccountStatus accountStatus;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<AccountBalanceEntity> saldi;


    public AccountEntity(String email,List<AccountBalanceEntity> saldi)
    {
        this.email = email;
        this.saldi = saldi;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public AccountEntity(String email)
    {
        this.email = email;
        this.accountStatus = AccountStatus.ACTIVE;
        this.saldi = new ArrayList<>();
    }

    protected AccountEntity(){}

    public void changeAccountStatus(AccountStatus accountStatus)
    {
        this.accountStatus = accountStatus;
    }

    public UUID getAccountId()
    {
        return accountId;
    }

    public String getEmail()
    {
        return email;
    }

    public AccountStatus getAccountStatus()
    {
        return accountStatus;
    }

    public List<AccountBalanceEntity> getSaldi()
    {
        return saldi;
    }

}

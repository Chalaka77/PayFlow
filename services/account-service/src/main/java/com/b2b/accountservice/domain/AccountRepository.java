package com.b2b.accountservice.domain;

import com.b2b.accountservice.api.dto.AccountResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID>
{
    Optional<AccountEntity> findByEmail(String email);
}

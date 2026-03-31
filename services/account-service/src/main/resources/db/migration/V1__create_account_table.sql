CREATE TABLE account (
    account_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(50) NOT NULL,
    status      VARCHAR(20) NOT NULL CHECK (status IN ('ENABLED', 'DISABLED', 'BLOCKED'))
);

CREATE TABLE account_balance (
    balance_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID NOT NULL REFERENCES account(account_id),
    currency    VARCHAR(3) NOT NULL,
    amount      DECIMAL(19,4) NOT NULL DEFAULT 0,
    UNIQUE (account_id, currency)
);
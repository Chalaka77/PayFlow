CREATE TABLE notification (
    notification_id   UUID PRIMARY KEY,
    payment_id        UUID NULL,        -- presente solo se accepted
    account_id        UUID NOT NULL,    -- il sender che riceve la notifica
    message           TEXT NOT NULL,
    sent_at           TIMESTAMP NOT NULL,
    status_payment              VARCHAR(20) NOT NULL  -- ACCEPTED / REJECTED
)
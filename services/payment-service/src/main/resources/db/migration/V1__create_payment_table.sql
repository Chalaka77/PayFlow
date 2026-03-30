CREATE TABLE payment (
    payment_id	UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_account_id UUID NOT NULL,
    receiver_account_id UUID NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    payment_requested_at TIMESTAMPTZ NOT NULL,
    reason_payment VARCHAR(255),
    updated_at TIMESTAMPTZ
)
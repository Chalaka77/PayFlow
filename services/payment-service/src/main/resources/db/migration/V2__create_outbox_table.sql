CREATE TABLE outbox (
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    topic        TEXT NOT NULL,
    event_type   TEXT NOT NULL,
    aggregate_id TEXT NOT NULL,
    payload_json JSONB NOT NULL,
    status       TEXT NOT NULL DEFAULT 'NEW',
    sent_at      TIMESTAMPTZ
);

CREATE INDEX idx_outbox_status_created ON outbox(status, created_at);
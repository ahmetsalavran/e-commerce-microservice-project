CREATE TABLE IF NOT EXISTS payments (
  payment_id UUID PRIMARY KEY,
  order_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  amount NUMERIC(18,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE payments
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE TABLE IF NOT EXISTS processed_event (
  event_id VARCHAR(80) PRIMARY KEY,
  event_type VARCHAR(40) NOT NULL,
  order_id BIGINT NOT NULL,
  status VARCHAR(200) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payment_account_event (
  id BIGSERIAL PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  order_id BIGINT,
  correlation_id VARCHAR(80) NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE payment_account_event
  DROP COLUMN IF EXISTS balance_after;

CREATE TABLE IF NOT EXISTS payment_account_event_archive (
  archive_id BIGSERIAL PRIMARY KEY,
  original_id BIGINT NOT NULL UNIQUE,
  customer_id BIGINT NOT NULL,
  order_id BIGINT,
  correlation_id VARCHAR(80) NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  archived_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE payment_account_event_archive
  DROP COLUMN IF EXISTS balance_after;

CREATE INDEX IF NOT EXISTS ix_payment_account_event_customer_id_created
  ON payment_account_event(customer_id, created_at);

CREATE INDEX IF NOT EXISTS ix_payment_account_event_order_id
  ON payment_account_event(order_id);

CREATE INDEX IF NOT EXISTS ix_payment_account_event_archive_customer_id_created
  ON payment_account_event_archive(customer_id, created_at);

CREATE INDEX IF NOT EXISTS ix_payment_account_event_archive_archived_at
  ON payment_account_event_archive(archived_at);

CREATE TABLE IF NOT EXISTS shedlock (
  name VARCHAR(64) PRIMARY KEY,
  lock_until TIMESTAMPTZ NOT NULL,
  locked_at TIMESTAMPTZ NOT NULL,
  locked_by VARCHAR(255) NOT NULL
);

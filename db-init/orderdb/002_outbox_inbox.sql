CREATE TABLE IF NOT EXISTS outbox_event (
  id BIGSERIAL PRIMARY KEY,
  aggregate_type VARCHAR(50) NOT NULL,
  aggregate_id VARCHAR(100) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  key VARCHAR(100),
  payload TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  retries INTEGER NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  available_at TIMESTAMPTZ NOT NULL,
  sent_at TIMESTAMPTZ,
  order_type VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS outbox_events (
  id             BIGSERIAL PRIMARY KEY,
  event_id       UUID NOT NULL,
  aggregate_type VARCHAR(50) NOT NULL,
  aggregate_id   BIGINT NOT NULL,
  topic          VARCHAR(100) NOT NULL,
  event_type     VARCHAR(100) NOT NULL,
  payload        JSONB NOT NULL,
  status         VARCHAR(20) NOT NULL DEFAULT 'NEW',
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  sent_at        TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_outbox_event_id ON outbox_events(event_id);
CREATE INDEX IF NOT EXISTS ix_outbox_status_created ON outbox_events(status, created_at);

CREATE TABLE IF NOT EXISTS inbox_events (
  id          BIGSERIAL PRIMARY KEY,
  event_id    UUID NOT NULL,
  source      VARCHAR(50) NOT NULL,
  event_type  VARCHAR(100) NOT NULL,
  received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_inbox_event_id ON inbox_events(event_id);

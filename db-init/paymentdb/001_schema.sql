CREATE TABLE payments (
  payment_id UUID PRIMARY KEY,
  order_id UUID NOT NULL,
  status VARCHAR(32) NOT NULL,
  amount NUMERIC(18,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_profile (
  id BIGSERIAL PRIMARY KEY,
  customer_id BIGINT NOT NULL UNIQUE,
  first_name VARCHAR(120) NOT NULL,
  last_name VARCHAR(120) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone VARCHAR(32) NOT NULL,
  inventory_strategy VARCHAR(40) NOT NULL,
  payment_strategy VARCHAR(40) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payment_method (
  id BIGSERIAL PRIMARY KEY,
  user_profile_id BIGINT NOT NULL REFERENCES user_profile(id),
  type VARCHAR(20) NOT NULL,
  provider_ref VARCHAR(120) NOT NULL,
  masked_value VARCHAR(120) NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT false,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_order_info (
  id BIGSERIAL PRIMARY KEY,
  user_profile_id BIGINT NOT NULL REFERENCES user_profile(id),
  order_id BIGINT NOT NULL UNIQUE,
  order_status VARCHAR(40) NOT NULL,
  selected_strategy VARCHAR(40) NOT NULL,
  total_price NUMERIC(19,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

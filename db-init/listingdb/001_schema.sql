CREATE TABLE IF NOT EXISTS products (
  product_id BIGSERIAL PRIMARY KEY,
  sku        VARCHAR(64)  NOT NULL UNIQUE,
  name       VARCHAR(255) NOT NULL,
  base_price NUMERIC(18,2) NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

TRUNCATE TABLE products RESTART IDENTITY;

INSERT INTO products (product_id, sku, name, base_price, updated_at)
SELECT
  gs::bigint AS product_id,
  'PRD-' || lpad(gs::text, 3, '0') AS sku,
  'Product ' || gs AS name,
  (50 + random() * 4950)::numeric(18,2) AS base_price,
  now() AS updated_at
FROM generate_series(1, 500) gs;

SELECT setval(
  pg_get_serial_sequence('products', 'product_id'),
  COALESCE((SELECT MAX(product_id) FROM products), 1),
  true
);

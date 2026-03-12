BEGIN;

TRUNCATE TABLE product_stock;

INSERT INTO product_stock (product_id, available, reserved, updated_at, version)
SELECT
  gs::bigint AS product_id,
  CASE
    WHEN random() < 0.15 THEN 0                              -- %15 ürün stok 0
    ELSE floor(500 + random() * 4501)::int                  -- 500..5000
  END AS available,
  0 AS reserved,
  now() AS updated_at,
  0 AS version
FROM generate_series(1, 150) gs;

COMMIT;

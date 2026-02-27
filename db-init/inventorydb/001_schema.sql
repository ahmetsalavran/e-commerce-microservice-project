CREATE TABLE product_stock (
  product_id UUID PRIMARY KEY,
  available INT NOT NULL CHECK (available >= 0),
  reserved INT NOT NULL DEFAULT 0 CHECK (reserved >= 0)
);

INSERT INTO product_stock(product_id, available, reserved)
VALUES ('11111111-1111-1111-1111-111111111111',1,0)
ON CONFLICT DO NOTHING;

CREATE TABLE products (
  product_id BIGSERIAL PRIMARY KEY,
  sku        VARCHAR(64)  NOT NULL UNIQUE,
  name       VARCHAR(255) NOT NULL,
  base_price NUMERIC(18,2) NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO product_listing (sku, name, base_price) VALUES
('FRUIT-001','Muz',10.00),
('FRUIT-002','Elma',8.50),
('FRUIT-003','Portakal',9.00),
('FRUIT-004','Mandarin',7.75),
('FRUIT-005','Çilek',18.00),

('VEG-001','Domates',6.50),
('VEG-002','Salatalık',5.25),
('VEG-003','Biber',7.90),
('VEG-004','Patlıcan',6.75),
('VEG-005','Kabak',5.95),

('DAIRY-001','Süt 1L',17.50),
('DAIRY-002','Yoğurt 1kg',22.00),
('DAIRY-003','Beyaz Peynir',95.00),
('DAIRY-004','Kaşar Peyniri',110.00),
('DAIRY-005','Tereyağı',140.00),

('MEAT-001','Tavuk Göğsü',85.00),
('MEAT-002','Dana Kıyma',320.00),
('MEAT-003','Kuzu Pirzola',450.00),
('MEAT-004','Sucuk',180.00),
('MEAT-005','Salam',120.00);

# Manual Service-by-Service Test Scripts

Bu klasor, servisleri IntelliJ'den tek tek kaldirip test etmek icin duzenlendi.

## Klasorler
- `order/`: Kafka'ya direkt `order.confirmed` publish scriptleri (strateji bazli)
- `inventory/`: inventory DB kontrol scriptleri
- `payment/`: payment DB kontrol/topup scriptleri
- `listing/`: listing DB kontrol scriptleri
- `user/`: user strateji/profile kontrol scriptleri
- `kafka/`: topic dinleme/listeleme scriptleri

## Hızlı Akış
1. Dinleyici ac (opsiyonel):
   - `./scripts/manual-tests/kafka/01-listen-all.sh`
2. Order event bas:
   - `./scripts/manual-tests/order/01-aon-success.sh`
   - script ciktisindan `orderId` ve `eventId` al
3. Inventory kontrol:
   - `./scripts/manual-tests/inventory/01-check-by-order.sh <orderId>`
4. Payment kontrol:
   - `./scripts/manual-tests/payment/02-check-by-event.sh <eventId>`
   - `./scripts/manual-tests/payment/01-check-balance.sh 1002`
5. Listing kontrol:
   - `./scripts/manual-tests/listing/01-check-products.sh`

## Order Scriptleri (tek tek)
- `01-aon-success.sh`
- `02-aon-fail.sh`
- `03-partial-success.sh`
- `04-partial-fail.sh`
- `05-partitioned-success.sh`
- `06-partitioned-fail.sh`

## Ortam Degiskenleri
- `BROKER` varsayilan: `localhost:29092`
- `ORDER_TOPIC` varsayilan: `order.confirmed`
- `CUSTOMER_ID` (order scriptlerinde override edilebilir)
- `PAYMENT_STRATEGY` varsayilan: `LOCAL_ONLY`

Ornek:
```bash
CUSTOMER_ID=1002 PAYMENT_STRATEGY=LOCAL_ONLY ./scripts/manual-tests/order/05-partitioned-success.sh
```

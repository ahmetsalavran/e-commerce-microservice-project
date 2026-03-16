# e-commerce-microservice-project

Bu repo, event-driven e-commerce akislarini gostermek icin hazirlanmis cok modullu bir Spring Boot mikroservis projesidir.

## Moduller

- `common-contract`: servisler arasi paylasilan event ve DTO contract'lari
- `order-service`: siparis olusturma, onaylama, iptal ve compensating action akislari
- `inventory-service`: stok dusumu, stok reddi ve inventory stratejileri
- `payment-service`: odeme isleme, duplicate event korumasi ve cleanup job'lari
- `product-listing-service`: urun listeleme ve fiyat/veri okuma
- `user-service`: kullanici profil ve strateji bilgileri

## Core Infrastructure kullanimi

Bu proje, ortak guvenilirlik ve web altyapisini `com.ms:core-infrastructure` kutuphanesi uzerinden kullanir.

Kutuphanenin sagladigi baslica kabiliyetler:

- `idempotency`: duplicate event'lerin ikinci kez islenmesini engeller
- `outbox`: DB transaction ile event publish niyetini guvenli sekilde ayirir
- `after-commit`: ack ve publish islemlerini commit sonrasina tasir
- `web infra`: ortak request-id ve exception handling davranisi saglar

## E-commerce akislarinda ne yapiyor?

Bu kutuphane proje icinde su tip problemlere karsi kullanilir:

- ayni siparis event'i tekrar geldiginde stok veya odeme isleminin ikinci kez calismamasi
- is verisi DB'ye yazildiktan sonra Kafka publish basarisiz olursa event niyetinin kaybolmamasi
- transaction tamamlanmadan `ack` verilmesi nedeniyle veri tutarsizligi olusmamasi
- order, inventory ve payment servislerinde ayni reliability mantiginin tekrar yazilmamasi

## Servis bazli kullanim

### order-service

- siparis onayi sirasinda `OutboxEventService` ile `outbox_event` kaydi olusturur
- `OutboxCreatedEvent` publish ederek commit sonrasi queue'ya gecis saglar
- `OutboxPublisherService`, outbox kaydini Kafka'ya basip `SENT/FAILED` durumunu gunceller
- inventory veya odeme tarafindan gelen duplicate event'leri `@Idempotent` ile ikinci kez isletmez

### inventory-service

- `OrderConfirmedEvent` duplicate gelirse stok ikinci kez dusmez
- basarili akista `PAYMENT_CHARGE_REQUESTED`, basarisiz akista `INVENTORY_REJECTED` outbox event'i olusturur
- `AfterCommitExecutor` ile ack ve event publish davranisini transaction sonrasina tasir

### payment-service

- duplicate odeme event'lerinde ayni tahsilat etkisinin ikinci kez olusmasini engeller
- cleanup job ile `processed_event` gibi tablolarin buyumesini kontrol eder
- farkli odeme stratejilerini infrastructure yardimcilariyla birlikte kullanir

### product-listing-service ve user-service

- outbox publisher implement etmezler
- buna ragmen ortak request-id ve exception handling altyapisini kullanirlar
- infrastructure kutuphanesi bu servislerde gereksiz worker bean'leri olusturmaz

## Calisma notlari

- servisler PostgreSQL ve Kafka ile calisir
- DB schema'lari `db-init/` altindadir
- local gelistirme icin `docker-compose.yml` kullanilabilir
- `core-infrastructure` artifact'i local Maven repo'ya kurulmus olmali

## Dikkat edilmesi gerekenler

- `outbox_event` ve `processed_event` tablo semalari kutuphane entity'leri ile uyumlu olmalidir
- publisher olmayan servislerde `OutboxJobPublisher` bean'i tanimlanmamalidir
- duplicate event korumasi icin `@Idempotent` anotasyonu olan methodlar transaction icinde calismalidir

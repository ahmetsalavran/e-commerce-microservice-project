# ars-core-infrastructure (Idempotency)

## Idea
Idempotency is checked in the aspect, but ACK behavior is service-specific:
- If called from Kafka, service passes `ackAfterCommit` and uses `IdempotencyContext.isDuplicate()` to early-return & ack.
- If not Kafka, service passes `null` and just runs/returns without ACK concerns.

## Service-side usage
```java
@Idempotent(key="#event.eventId", eventType="#event.eventType", orderId="#event.orderId")
@Transactional
public void handle(Event event, Runnable ackAfterCommit) {

  if (ackAfterCommit != null && IdempotencyContext.isDuplicate()) {
    TransactionSynchronizationManager.registerSynchronization(afterCommit(ackAfterCommit));
    return;
  }

  // business logic...

  if (ackAfterCommit != null) {
    TransactionSynchronizationManager.registerSynchronization(afterCommit(ackAfterCommit));
  }
}
```

IMPORTANT: `IdempotencyContext` is ThreadLocal and cleared by the aspect.

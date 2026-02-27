package com.ars.order.messaging.inner;

import com.ars.order.messaging.outer.OutboxJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxAfterCommitListener {

    private final OutboxQueue outboxQueue;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxCreated(OutboxCreatedEvent e) {
        outboxQueue.enqueue(new OutboxJob(
                e.outboxRowId(),
                e.eventType(),
                e.key(),
                e.payload()
        ));
    }
}
package com.ars.inventory.messaging.inner;

import com.ars.inventory.messaging.publishers.OutboxJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxAfterCommitListener {

    private final OutboxQueue outboxQueue;

    /**
     * BU SERVİS ORDER A BAŞARISIZ BİLGİSİNİ VERECEK BANA BURDAN KAFKAYA PUBLİSH EDİCEM ORDER GERİ DÖNDÜRECEK */
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
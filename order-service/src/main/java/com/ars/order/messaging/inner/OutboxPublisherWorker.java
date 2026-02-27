package com.ars.order.messaging.inner;

import com.ars.order.messaging.outer.OutboxJob;
import com.ars.order.messaging.outer.OutboxPublisherService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisherWorker {

    private final OutboxQueue queue;
    private final OutboxPublisherService publisher;

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::loop, "outbox-publisher");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                OutboxJob job = queue.take();
                publisher.publishFastPath(job); // ✅ DB read yok
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                // log
            }
        }
    }
}

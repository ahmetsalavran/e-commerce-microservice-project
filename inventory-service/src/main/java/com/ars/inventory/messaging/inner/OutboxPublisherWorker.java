package com.ars.inventory.messaging.inner;

import com.ars.inventory.messaging.publishers.OutboxJob;
import com.ars.inventory.messaging.publishers.OutboxPublisherService;
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
        // BUNU ŞİMDİLİK KALDIRIYORUZ
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

package com.ars.inventory.messaging.inner;

import com.ars.inventory.messaging.publishers.OutboxJob;
import org.springframework.stereotype.Component;

@Component
public class OutboxQueue {
    private final java.util.concurrent.BlockingQueue<OutboxJob> q = new java.util.concurrent.LinkedBlockingQueue<>();

    public void enqueue(OutboxJob job) { q.offer(job); }
    public OutboxJob take() throws InterruptedException { return q.take(); }
}
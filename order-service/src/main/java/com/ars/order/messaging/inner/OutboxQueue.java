package com.ars.order.messaging.inner;

import com.ars.order.messaging.outer.OutboxJob;
import org.springframework.stereotype.Component;

@Component
public class OutboxQueue {
    private final java.util.concurrent.BlockingQueue<OutboxJob> q = new java.util.concurrent.LinkedBlockingQueue<>();

    public void enqueue(OutboxJob job) { q.offer(job); }
    public OutboxJob take() throws InterruptedException { return q.take(); }
}
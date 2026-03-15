package com.ars.payment.jobs;

import com.ms.core.infrastructure.idempotency.repo.ProcessedEventRepository;
import com.ars.payment.repository.PaymentAccountEventRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;

@Component
public class PaymentEventCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventCleanupJob.class);

    private final PaymentAccountEventRepository paymentAccountEventRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Value("${app.payment.cleanup.enabled:true}")
    private boolean enabled;

    @Value("${app.payment.cleanup.retention-days:14}")
    private int retentionDays;

    @Value("${app.payment.cleanup.batch-size:1000}")
    private int batchSize;

    @Value("${app.payment.cleanup.max-batches-per-run:200}")
    private int maxBatchesPerRun;

    public PaymentEventCleanupJob(PaymentAccountEventRepository paymentAccountEventRepository,
                                  ProcessedEventRepository processedEventRepository) {
        this.paymentAccountEventRepository = paymentAccountEventRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Scheduled(
            cron = "${app.payment.cleanup.cron:0 0 3 * * *}",
            zone = "${app.payment.cleanup.zone:Europe/Istanbul}"
    )
    @SchedulerLock(
            name = "paymentEventCleanupJob",
            lockAtMostFor = "${app.payment.cleanup.lock-at-most-for:PT30M}",
            lockAtLeastFor = "${app.payment.cleanup.lock-at-least-for:PT10S}"
    )
    public void runNightlyCleanup() {
        if (!enabled) {
            return;
        }
        int safeBatchSize = Math.max(batchSize, 1);
        int safeMaxBatches = Math.max(maxBatchesPerRun, 1);

        OffsetDateTime paymentCutoff = OffsetDateTime.now().minusDays(Math.max(retentionDays, 1));
        Instant processedCutoff = paymentCutoff.toInstant();

        int paymentArchived = archivePaymentEventBatches(paymentCutoff, safeBatchSize, safeMaxBatches);
        int carryForwardInserted = paymentAccountEventRepository.insertCarryForwardForCustomersWithoutActiveEvents(paymentCutoff);
        int processedDeleted = deleteProcessedEventBatches(processedCutoff, safeBatchSize, safeMaxBatches);

        log.info("payment cleanup completed: payment_account_event_archived={}, carry_forward_inserted={}, processed_event_deleted={}, retentionDays={}, batchSize={}, maxBatchesPerRun={}",
                paymentArchived, carryForwardInserted, processedDeleted, retentionDays, safeBatchSize, safeMaxBatches);
        int hardLimit = safeBatchSize * safeMaxBatches;
        if (paymentArchived >= hardLimit || processedDeleted >= hardLimit) {
            log.warn("payment cleanup reached max batch limit in this run; consider increasing app.payment.cleanup.max-batches-per-run or reducing retention days");
        }
    }

    protected int archivePaymentEventBatches(OffsetDateTime cutoff, int safeBatchSize, int safeMaxBatches) {
        int total = 0;
        for (int i = 0; i < safeMaxBatches; i++) {
            int archived = paymentAccountEventRepository.archiveAndDeleteBatchOlderThan(cutoff, safeBatchSize);
            total += archived;
            if (archived < safeBatchSize) {
                break;
            }
        }
        return total;
    }

    protected int deleteProcessedEventBatches(Instant cutoff, int safeBatchSize, int safeMaxBatches) {
        int total = 0;
        for (int i = 0; i < safeMaxBatches; i++) {
            int deleted = processedEventRepository.deleteBatchOlderThan(cutoff, safeBatchSize);
            total += deleted;
            if (deleted < safeBatchSize) {
                break;
            }
        }
        return total;
    }
}

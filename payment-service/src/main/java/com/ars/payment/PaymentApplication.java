package com.ars.payment;

import com.ms.core.infrastructure.persistence.InfrastructureEntitiesMarker;
import com.ms.core.infrastructure.persistence.InfrastructureRepositoriesMarker;
import com.ars.payment.entity.PaymentAccountEvent;
import com.ars.payment.repository.PaymentAccountEventRepository;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "${app.payment.cleanup.lock-at-most-for:PT30M}")
@EnableJpaRepositories(basePackageClasses = {
        PaymentAccountEventRepository.class,
        InfrastructureRepositoriesMarker.class
})
@EntityScan(basePackageClasses = {
        PaymentAccountEvent.class,
        InfrastructureEntitiesMarker.class
})
public class PaymentApplication {
  public static void main(String[] args) {
    SpringApplication.run(PaymentApplication.class, args);
  }
}

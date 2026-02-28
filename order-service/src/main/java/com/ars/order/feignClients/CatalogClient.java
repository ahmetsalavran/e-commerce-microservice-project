package com.ars.order.feignClients;

import com.ars.contract.catalog.GetProductPricesRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;


@FeignClient(name = "catalog-service", url = "${clients.catalog.url}")
public interface CatalogClient {

    @PostMapping("/productPrices")
    Map<Long, BigDecimal> getProductPrices(@RequestBody GetProductPricesRequest request);
}

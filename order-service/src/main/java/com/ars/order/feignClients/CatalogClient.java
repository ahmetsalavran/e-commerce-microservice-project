package com.ars.order.feignClients;

import com.ars.contract.catalog.GetProductPricesRequest;
import com.ars.contract.catalog.ProductPriceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@FeignClient(name = "catalog-service", url = "http://localhost:8084")
public interface CatalogClient {

    @PostMapping("/productPrices")
    Map<Long, BigDecimal> getProductPrices(@RequestBody GetProductPricesRequest request);
}
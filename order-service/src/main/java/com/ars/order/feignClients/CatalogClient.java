package com.ars.order.feignClients;

import com.ars.contract.catalog.GetProductPricesRequest;
import com.ars.contract.catalog.ProductPriceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "catalog-service", url = "http://localhost:8084")
public interface CatalogClient {

    @PostMapping("/productPrices")
    List<ProductPriceDto> getProductPrices(@RequestBody GetProductPricesRequest request);
}
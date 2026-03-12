package com.ars.listing.service;

import com.ars.listing.model.dto.ProductDto;
import com.ars.listing.model.request.ProductCreateRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService {
    List<ProductDto> productList();

    void createProduct(ProductCreateRequest request);

    Map<Long, BigDecimal> getProductPrices(List<Long> ids);

    void markAvailableNegative(List<Long> ids);
}


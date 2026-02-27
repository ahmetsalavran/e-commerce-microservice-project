package com.ars.listing.service;

import com.ars.contract.catalog.ProductPriceDto;
import com.ars.listing.dto.ProductDto;
import com.ars.listing.model.request.ProductCreateRequest;

import java.util.List;

public interface ProductService {
    List<ProductDto> productList();

    void createProduct(ProductCreateRequest request);

    List<ProductPriceDto> getProductPrices(List<Long> productIds);
}

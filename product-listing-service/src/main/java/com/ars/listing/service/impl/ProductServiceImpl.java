package com.ars.listing.service.impl;

import com.ars.contract.catalog.ProductPriceDto;
import com.ars.contract.catalog.ProductPriceProjection;
import com.ars.listing.dto.ProductDto;
import com.ars.listing.entity.Product;
import com.ars.listing.model.request.ProductCreateRequest;
import com.ars.listing.repositories.ProductRepository;
import com.ars.listing.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDto> productList() {
        return productRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createProduct(ProductCreateRequest request) {

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setBasePrice(request.getBasePrice());
        product.setUpdatedAt(OffsetDateTime.now());
        productRepository.save(product);
    }


    @Override
    public Map<Long, BigDecimal> getProductPrices(List<Long> productIds) {

        return productRepository.findPricesByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductPriceProjection::getProductId,
                        ProductPriceProjection::getBasePrice
                ));
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setBasePrice(product.getBasePrice());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}

package com.ars.listing.controller;

import com.ars.contract.catalog.GetProductPricesRequest;
import com.ars.contract.catalog.ProductPriceDto;
import com.ars.listing.dto.ProductDto;
import com.ars.listing.model.request.ProductCreateRequest;
import com.ars.listing.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductDto> products() {
        return productService.productList();
    }

    @GetMapping("/ok")
    public String ok() {
        return "ok";
    }

    @PostMapping("/products")
    public void createProduct(@RequestBody ProductCreateRequest request) {
        productService.createProduct(request);
    }

    @PostMapping("/productPrices")
    public List<ProductPriceDto> getProductPrices(@RequestBody GetProductPricesRequest request) {
        return productService.getProductPrices(request.productIds());
    }

}

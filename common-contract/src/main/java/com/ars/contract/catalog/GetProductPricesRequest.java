package com.ars.contract.catalog;

import java.util.List;

public record GetProductPricesRequest(List<Long> productIds) {
}
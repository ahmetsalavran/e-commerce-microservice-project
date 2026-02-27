package com.ars.inventory.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "product_stock")
@Getter
@Setter
public class ProductStock {

    @Id
    @Column(name = "product_id", length = 64)
    private Long productId;

    @Column(nullable = false)
    private Integer available;

    @Column(nullable = false)
    private Integer reserved = 0;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // Extra güvenlik için optimistic lock
    @Version
    private Long version;
}

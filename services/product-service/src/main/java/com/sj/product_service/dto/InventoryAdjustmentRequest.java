package com.sj.product_service.dto;

import lombok.Data;

@Data
public class InventoryAdjustmentRequest {
    private int quantityDelta;
}

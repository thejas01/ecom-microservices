package com.ecommerce.common.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionsDTO {
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String unit; // CM, INCH
}
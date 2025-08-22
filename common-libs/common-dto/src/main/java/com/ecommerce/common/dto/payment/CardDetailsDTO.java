package com.ecommerce.common.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDetailsDTO {
    
    private String lastFourDigits;
    
    private String cardBrand; // VISA, MASTERCARD, AMEX, etc.
    
    private String cardholderName;
    
    private String expiryMonth;
    
    private String expiryYear;
    
    private String postalCode;
}
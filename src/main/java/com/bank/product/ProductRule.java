package com.bank.product;

import java.math.BigDecimal;

public interface ProductRule {

    void validateAge(String productCode, int age);

    void validateTransactionAmount(String productCode, BigDecimal amount);
    
    boolean isValidProductCode(String productCode);
}

package com.bank.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.bank.exception.InvalidProductException;

@ApplicationScoped
public class ProductRuleFactory {

    @Inject
    SalaryProductRule salaryProductRule;

    @Inject
    StudentProductRule studentProductRule;

    public ProductRule getRule(String productType) {
        if (productType == null || productType.isBlank()) {
            throw new InvalidProductException("productType is required");
        }
        switch (productType.toUpperCase()) {
            case "SALARY":
                return salaryProductRule;
            case "STUDENT":
                return studentProductRule;
            default:
                throw new InvalidProductException("Unknown product type: " + productType);
        }
    }
}

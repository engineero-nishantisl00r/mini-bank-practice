package com.bank.product;

import com.bank.config.SalaryProductConfig;
import com.bank.exception.AgeRuleViolationException;
import com.bank.exception.AmountRuleViolationException;
import com.bank.exception.InvalidProductException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;

@ApplicationScoped
public class SalaryProductRule implements ProductRule {

    @Inject
    SalaryProductConfig config;

    @Override
    public void validateAge(String productCode, int age) {
        SalaryProductConfig.SalaryRule rule = config.rules()
                .stream()
                .filter(r -> r.code().equals(productCode))
                .findFirst()
                .orElseThrow(() -> new InvalidProductException("Product code not found: " + productCode));

        if (age < rule.minAge()) {
            throw new AgeRuleViolationException(
                    "Age " + age + " is below minimum age " + rule.minAge() + " for Salary product " + productCode);
        }
    }

    @Override
    public void validateTransactionAmount(String productCode, BigDecimal amount) {
        SalaryProductConfig.SalaryRule rule = config.rules()
                .stream()
                .filter(r -> r.code().equals(productCode))
                .findFirst()
                .orElseThrow(() -> new InvalidProductException("Product code not found: " + productCode));

        if (amount.compareTo(BigDecimal.valueOf(rule.minTransactionAmount())) < 0) {
            throw new AmountRuleViolationException(
                    "Transaction amount " + amount + " is below minimum " + rule.minTransactionAmount()
                            + " for Salary product " + productCode);
        }
    }

    @Override
    public boolean isValidProductCode(String productCode) {
        return config.rules()
                .stream()
                .anyMatch(rule -> rule.code().equals(productCode));
    }
}

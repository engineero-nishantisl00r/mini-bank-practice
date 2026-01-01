package com.bank.product;

import com.bank.config.StudentProductConfig;
import com.bank.exception.AgeRuleViolationException;
import com.bank.exception.AmountRuleViolationException;
import com.bank.exception.InvalidProductException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;

@ApplicationScoped
public class StudentProductRule implements ProductRule {

    @Inject
    StudentProductConfig config;

    @Override
    public void validateAge(String productCode, int age) {
        StudentProductConfig.StudentRule rule = config.rules()
                .stream()
                .filter(r -> r.code().equals(productCode))
                .findFirst()
                .orElseThrow(() -> new InvalidProductException("Product code not found: " + productCode));

        if (age > rule.maxAge()) {
            throw new AgeRuleViolationException(
                    "Age " + age + " exceeds maximum age " + rule.maxAge() + " for Student product " + productCode);
        }
    }

    @Override
    public void validateTransactionAmount(String productCode, BigDecimal amount) {
        StudentProductConfig.StudentRule rule = config.rules()
                .stream()
                .filter(r -> r.code().equals(productCode))
                .findFirst()
                .orElseThrow(() -> new InvalidProductException("Product code not found: " + productCode));

        if (amount.compareTo(BigDecimal.valueOf(rule.maxTransactionAmount())) > 0) {
            throw new AmountRuleViolationException(
                    "Transaction amount " + amount + " exceeds maximum " + rule.maxTransactionAmount()
                            + " for Student product " + productCode);
        }
    }

    @Override
    public boolean isValidProductCode(String productCode) {
        return config.rules()
                .stream()
                .anyMatch(rule -> rule.code().equals(productCode));
    }
}

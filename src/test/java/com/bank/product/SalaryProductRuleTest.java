package com.bank.product;

import com.bank.config.SalaryProductConfig;
import com.bank.exception.AgeRuleViolationException;
import com.bank.exception.AmountRuleViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalaryProductRuleTest {

    @Test
    void validateAge_and_transactionAmount() {
        SalaryProductRule rule = new SalaryProductRule();

        SalaryProductConfig config = new SalaryProductConfig() {
            @Override
            public List<SalaryRule> rules() {
                return List.of(new SalaryRule() {
                    @Override
                    public String code() {
                        return "SAL1";
                    }

                    @Override
                    public int minAge() {
                        return 18;
                    }

                    @Override
                    public int minTransactionAmount() {
                        return 10;
                    }
                });
            }
        };

        rule.config = config;

        // valid age
        assertDoesNotThrow(() -> rule.validateAge("SAL1", 20));

        // invalid age
        assertThrows(AgeRuleViolationException.class, () -> rule.validateAge("SAL1", 16));

        // valid amount
        assertDoesNotThrow(() -> rule.validateTransactionAmount("SAL1", BigDecimal.valueOf(20)));

        // invalid amount
        assertThrows(AmountRuleViolationException.class, () -> rule.validateTransactionAmount("SAL1", BigDecimal.valueOf(5)));
        
        // test isValidProductCode
        assertTrue(rule.isValidProductCode("SAL1"));
        assertFalse(rule.isValidProductCode("INVALID"));
    }
}

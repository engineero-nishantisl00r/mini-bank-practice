package com.bank.product;

import com.bank.config.StudentProductConfig;
import com.bank.exception.AgeRuleViolationException;
import com.bank.exception.AmountRuleViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentProductRuleTest {

    @Test
    void validateAge_and_transactionAmount() {
        StudentProductRule rule = new StudentProductRule();

        StudentProductConfig config = new StudentProductConfig() {
            @Override
            public List<StudentRule> rules() {
                return List.of(new StudentRule() {
                    @Override
                    public String code() {
                        return "STU1";
                    }

                    @Override
                    public int maxAge() {
                        return 25;
                    }

                    @Override
                    public int maxTransactionAmount() {
                        return 1000;
                    }
                });
            }
        };

        rule.config = config;

        // valid age
        assertDoesNotThrow(() -> rule.validateAge("STU1", 20));

        // invalid age
        assertThrows(AgeRuleViolationException.class, () -> rule.validateAge("STU1", 30));

        // valid amount
        assertDoesNotThrow(() -> rule.validateTransactionAmount("STU1", BigDecimal.valueOf(100)));

        // invalid amount
        assertThrows(AmountRuleViolationException.class, () -> rule.validateTransactionAmount("STU1", BigDecimal.valueOf(2000)));
        
        // test isValidProductCode
        assertTrue(rule.isValidProductCode("STU1"));
        assertFalse(rule.isValidProductCode("INVALID"));
    }
}

package com.bank.exception;

public class AmountRuleViolationException extends BusinessException {
    public AmountRuleViolationException(String message) {
        super(message);
    }
}

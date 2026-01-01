package com.bank.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {

    public String accountNumber;
    public String paymentType; // CREDIT or DEBIT
    public BigDecimal amount;
    public LocalDate date;
}

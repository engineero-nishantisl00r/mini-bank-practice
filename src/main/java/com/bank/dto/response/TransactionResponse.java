package com.bank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {

    public String postingNumber;
    public String accountNumber;
    public String paymentType;
    public BigDecimal amount;
    public LocalDate date;
}

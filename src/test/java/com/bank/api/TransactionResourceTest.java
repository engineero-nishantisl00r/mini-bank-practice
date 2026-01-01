package com.bank.api;

import com.bank.dto.request.TransactionRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.domain.entity.Posting;
import com.bank.exception.InsufficientBalanceException;
import com.bank.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionResourceTest {

    private TransactionService transactionService;
    private TransactionResource resource;

    @BeforeEach
    void setup() {
        transactionService = mock(TransactionService.class);
        resource = new TransactionResource();
        resource.transactionService = transactionService;
    }

    @Test
    void postAndGetTransaction_directInvocation() {
        TransactionRequest req = new TransactionRequest();
        req.accountNumber = "ACC_R1";
        req.paymentType = "CREDIT";
        req.amount = BigDecimal.valueOf(100);
        req.date = LocalDate.now();

        TransactionResponse resp = new TransactionResponse();
        resp.postingNumber = "P123";
        resp.accountNumber = req.accountNumber;
        resp.amount = req.amount;

        when(transactionService.postTransaction(req)).thenReturn(resp);
        when(transactionService.getTransaction("P123")).thenReturn(resp);

        TransactionResponse created = resource.postTransaction(req);
        assertEquals("ACC_R1", created.accountNumber);

        TransactionResponse fetched = resource.getTransaction("P123");
        assertEquals("P123", fetched.postingNumber);
    }

    @Test
    void postDebit_insufficientBalance_throws() {
        TransactionRequest req = new TransactionRequest();
        req.accountNumber = "ACC_R2";
        req.paymentType = "DEBIT";
        req.amount = BigDecimal.valueOf(50);

        when(transactionService.postTransaction(req)).thenThrow(new InsufficientBalanceException("Insufficient"));

        assertThrows(InsufficientBalanceException.class, () -> resource.postTransaction(req));
    }

}

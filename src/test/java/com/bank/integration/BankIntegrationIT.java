package com.bank.integration;

import com.bank.domain.entity.Account;
import com.bank.domain.entity.Balance;
import com.bank.domain.entity.Posting;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.BalanceRepository;
import com.bank.domain.repository.PostingRepository;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.TransactionRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.TransactionResponse;
import com.bank.service.AccountService;
import com.bank.service.TransactionService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class BankIntegrationIT {

    @Inject
    AccountService accountService;

    @Inject
    TransactionService transactionService;

    @Inject
    AccountRepository accountRepository;

    @Inject
    BalanceRepository balanceRepository;

    @Inject
    PostingRepository postingRepository;

    @BeforeEach
    void cleanup() {
        // No explicit cleanup required for in-memory DB with drop-and-create per test
        // JVM, but ensure unique ids
    }

    @Test
    public void createAccount_persistsEntityAndBalance() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "INT-ACC-1";
        req.productType = "SALARY";
        req.productCode = "SAL1";
        req.name = "IntegrationUser";
        req.age = 30;

        AccountResponse resp = accountService.createAccount(req);
        assertEquals("INT-ACC-1", resp.accountNumber);

        Optional<Account> a = accountRepository.findByAccountNumber("INT-ACC-1");
        assertTrue(a.isPresent());

        Optional<Balance> b = balanceRepository.findByAccount(a.get());
        assertTrue(b.isPresent());
        assertEquals(0, b.get().balance.compareTo(BigDecimal.ZERO));
    }

    @Test
    public void postTransaction_updatesBalanceAndCreatesPosting() {
        // ensure account exists
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "INT-ACC-2";
        req.productType = "SALARY";
        req.productCode = "SAL1";
        req.name = "TxnUser";
        req.age = 35;
        accountService.createAccount(req);

        TransactionRequest tr = new TransactionRequest();
        tr.accountNumber = "INT-ACC-2";
        tr.paymentType = "CREDIT";
        tr.amount = BigDecimal.valueOf(500);
        tr.date = LocalDate.now();

        TransactionResponse tresp = transactionService.postTransaction(tr);
        assertNotNull(tresp.postingNumber);

        Optional<Account> a = accountRepository.findByAccountNumber("INT-ACC-2");
        assertTrue(a.isPresent());

        Optional<Balance> b = balanceRepository.findByAccount(a.get());
        assertTrue(b.isPresent());
        assertEquals(0, b.get().balance.compareTo(BigDecimal.valueOf(500)));

        Optional<Posting> p = postingRepository.findByPostingNumber(tresp.postingNumber);
        assertTrue(p.isPresent());
        assertEquals(tresp.postingNumber, p.get().postingNumber);
    }

    @Test
    public void createStudentAccount_ageValidationFails() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "INT-STU-AGE";
        req.productType = "STUDENT";
        req.productCode = "STU1";
        req.name = "StudentUser";
        req.age = 30; // STU1 maxAge is 24 in config

        assertThrows(com.bank.exception.AgeRuleViolationException.class,
                () -> accountService.createAccount(req));
    }

    @Test
    public void postTransaction_studentAmountTooHigh_throws() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "INT-STU-AMT";
        req.productType = "STUDENT";
        req.productCode = "STU1";
        req.name = "StudentUser2";
        req.age = 20;
        accountService.createAccount(req);

        TransactionRequest tr = new TransactionRequest();
        tr.accountNumber = "INT-STU-AMT";
        tr.paymentType = "CREDIT";
        tr.amount = BigDecimal.valueOf(10000); // exceeds STU1 max 500
        tr.date = LocalDate.of(2025, 12, 3);

        assertThrows(com.bank.exception.AmountRuleViolationException.class,
                () -> transactionService.postTransaction(tr));
    }

    @Test
    public void postTransaction_salaryAmountTooLow_throws() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "INT-SAL-AMT";
        req.productType = "SALARY";
        req.productCode = "SAL1";
        req.name = "SalaryUser";
        req.age = 30;
        accountService.createAccount(req);

        TransactionRequest tr = new TransactionRequest();
        tr.accountNumber = "INT-SAL-AMT";
        tr.paymentType = "CREDIT";
        tr.amount = BigDecimal.valueOf(1); // below SAL1 min 100
        tr.date = LocalDate.of(2025, 12, 3);

        assertThrows(com.bank.exception.AmountRuleViolationException.class,
                () -> transactionService.postTransaction(tr));
    }

    @Test
    public void postTransaction_debit_insufficientFunds_integration() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "INT-DEBIT-INS";
        req.productType = "SALARY";
        req.productCode = "SAL1";
        req.name = "DebitUser";
        req.age = 40;
        accountService.createAccount(req);

        TransactionRequest credit = new TransactionRequest();
        credit.accountNumber = "INT-DEBIT-INS";
        credit.paymentType = "CREDIT";
        credit.amount = BigDecimal.valueOf(50);
        credit.date = LocalDate.of(2025, 12, 4);
        transactionService.postTransaction(credit);

        TransactionRequest debit = new TransactionRequest();
        debit.accountNumber = "INT-DEBIT-INS";
        debit.paymentType = "DEBIT";
        debit.amount = BigDecimal.valueOf(100);
        debit.date = LocalDate.of(2025, 12, 4);

        assertThrows(com.bank.exception.InsufficientBalanceException.class,
                () -> transactionService.postTransaction(debit));
    }

    @Test
    public void postingNumber_sequence_integration() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "INT-SEQ-1";
        req.productType = "SALARY";
        req.productCode = "SAL1";
        req.name = "SeqUser";
        req.age = 33;
        accountService.createAccount(req);

        LocalDate d = LocalDate.of(2030, 1, 1);

        TransactionRequest t1 = new TransactionRequest();
        t1.accountNumber = "INT-SEQ-1";
        t1.paymentType = "CREDIT";
        t1.amount = BigDecimal.valueOf(10);
        t1.date = d;

        TransactionRequest t2 = new TransactionRequest();
        t2.accountNumber = "INT-SEQ-1";
        t2.paymentType = "CREDIT";
        t2.amount = BigDecimal.valueOf(20);
        t2.date = d;

        TransactionResponse r1 = transactionService.postTransaction(t1);
        TransactionResponse r2 = transactionService.postTransaction(t2);

        assertNotNull(r1.postingNumber);
        assertNotNull(r2.postingNumber);
        assertTrue(r1.postingNumber.endsWith("-0001"));
        assertTrue(r2.postingNumber.endsWith("-0002"));
    }
}

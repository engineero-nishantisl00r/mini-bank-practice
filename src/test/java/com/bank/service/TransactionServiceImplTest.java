package com.bank.service;

import com.bank.domain.entity.Account;
import com.bank.domain.entity.Balance;
import com.bank.domain.entity.Posting;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.BalanceRepository;
import com.bank.domain.repository.PostingRepository;
import com.bank.dto.request.TransactionRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.exception.InsufficientBalanceException;
import com.bank.product.ProductRule;
import com.bank.product.ProductRuleFactory;
import com.bank.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    AccountRepository accountRepository;
    BalanceRepository balanceRepository;
    PostingRepository postingRepository;
    ProductRuleFactory productRuleFactory;
    TransactionServiceImpl service;

    @BeforeEach
    void setup() {
        accountRepository = mock(AccountRepository.class);
        balanceRepository = mock(BalanceRepository.class);
        postingRepository = mock(PostingRepository.class);
        productRuleFactory = mock(ProductRuleFactory.class);
        service = new TransactionServiceImpl(accountRepository, balanceRepository, postingRepository,
                productRuleFactory);
    }

    @Test
    void postTransaction_credit_updatesBalanceAndCreatesPosting() {
        Account acc = new Account();
        acc.accountNumber = "ACC3";
        acc.productType = "SALARY";
        acc.productCode = "SAL1";

        Balance bal = new Balance();
        bal.balance = BigDecimal.valueOf(50);

        when(accountRepository.findByAccountNumber("ACC3")).thenReturn(Optional.of(acc));
        when(balanceRepository.findByAccount(acc)).thenReturn(Optional.of(bal));

        TransactionRequest req = new TransactionRequest();
        req.accountNumber = "ACC3";
        req.paymentType = "CREDIT";
        req.amount = BigDecimal.valueOf(25);
        req.date = LocalDate.now();

        ProductRule rule = mock(ProductRule.class);
        when(productRuleFactory.getRule(acc.productType)).thenReturn(rule);
        doNothing().when(rule).validateTransactionAmount("SAL1", BigDecimal.valueOf(25));
        @SuppressWarnings("unchecked")
        io.quarkus.hibernate.orm.panache.PanacheQuery<Posting> query = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(postingRepository.find(eq("postingDate"), any(LocalDate.class))).thenReturn(query);
        when(query.list()).thenReturn(java.util.Collections.emptyList());
        doAnswer(invocation -> {
            Posting p = invocation.getArgument(0);
            p.id = 1L;
            return null;
        }).when(postingRepository).persist(any(Posting.class));

        TransactionResponse res = service.postTransaction(req);

        assertEquals("ACC3", res.accountNumber);
        assertEquals(0, res.amount.compareTo(BigDecimal.valueOf(25)));
        verify(balanceRepository).persist(bal);
        verify(postingRepository).persist(any(Posting.class));
    }

    @Test
    void postTransaction_debit_insufficientBalanceThrows() {
        Account acc = new Account();
        acc.accountNumber = "ACC4";
        acc.productType = "SALARY";
        acc.productCode = "SAL1";

        Balance bal = new Balance();
        bal.balance = BigDecimal.valueOf(10);

        when(accountRepository.findByAccountNumber("ACC4")).thenReturn(Optional.of(acc));
        when(balanceRepository.findByAccount(acc)).thenReturn(Optional.of(bal));

        TransactionRequest req = new TransactionRequest();
        req.accountNumber = "ACC4";
        req.paymentType = "DEBIT";
        req.amount = BigDecimal.valueOf(20);

        ProductRule rule = mock(ProductRule.class);
        when(productRuleFactory.getRule(acc.productType)).thenReturn(rule);
        doNothing().when(rule).validateTransactionAmount("SAL1", BigDecimal.valueOf(20));
        @SuppressWarnings("unchecked")
        io.quarkus.hibernate.orm.panache.PanacheQuery<Posting> query = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
        when(postingRepository.find(eq("postingDate"), any(LocalDate.class))).thenReturn(query);
        when(query.list()).thenReturn(java.util.Collections.emptyList());

        assertThrows(InsufficientBalanceException.class, () -> service.postTransaction(req));
    }

    @Test
    void getTransaction_success() {
        Posting p = new Posting();
        p.postingNumber = "P1";
        p.accountNumber = "ACC5";
        p.paymentType = "CREDIT";
        p.amount = BigDecimal.valueOf(77);
        p.postingDate = LocalDate.now();

        when(postingRepository.findByPostingNumber("P1")).thenReturn(Optional.of(p));

        TransactionResponse res = service.getTransaction("P1");
        assertEquals("P1", res.postingNumber);
        assertEquals(0, res.amount.compareTo(BigDecimal.valueOf(77)));
    }
}

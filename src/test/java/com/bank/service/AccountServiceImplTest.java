package com.bank.service;

import com.bank.domain.entity.Account;
import com.bank.domain.entity.Balance;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.BalanceRepository;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.product.ProductRule;
import com.bank.product.ProductRuleFactory;
import com.bank.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    AccountRepository accountRepository;
    BalanceRepository balanceRepository;
    ProductRuleFactory productRuleFactory;
    AccountServiceImpl service;

    @BeforeEach
    void setup() {
        accountRepository = mock(AccountRepository.class);
        balanceRepository = mock(BalanceRepository.class);
        productRuleFactory = mock(ProductRuleFactory.class);
        service = new AccountServiceImpl(accountRepository, balanceRepository, productRuleFactory);
    }

    @Test
    void createAccount_success() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "ACC1";
        req.productCode = "SAL1";
        req.productType = "SALARY";
        req.name = "Bob";
        req.age = 30;

        when(accountRepository.findByAccountNumber("ACC1")).thenReturn(Optional.empty());
        ProductRule rule = mock(ProductRule.class);
        when(productRuleFactory.getRule("SALARY")).thenReturn(rule);
        when(rule.isValidProductCode("SAL1")).thenReturn(true);
        doNothing().when(rule).validateAge("SAL1", 30);
        doAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.id = 1L;
            return null;
        }).when(accountRepository).persist(any(Account.class));
        doAnswer(invocation -> {
            return null;
        }).when(balanceRepository).persist(any(Balance.class));

        AccountResponse res = service.createAccount(req);

        assertEquals("ACC1", res.accountNumber);
        assertEquals("Bob", res.name);
        assertEquals(0, res.balance.compareTo(BigDecimal.ZERO));

        verify(accountRepository).persist(any(Account.class));
        verify(balanceRepository).persist(any(Balance.class));
    }

    @Test
    void getAccount_success() {
        Account acc = new Account();
        acc.accountNumber = "ACC2";
        acc.productCode = "P";
        acc.productType = "SALARY";
        acc.name = "Carol";
        acc.age = 22;

        Balance bal = new Balance();
        bal.balance = BigDecimal.valueOf(123.45);

        when(accountRepository.findByAccountNumber("ACC2")).thenReturn(Optional.of(acc));
        when(balanceRepository.findByAccount(acc)).thenReturn(Optional.of(bal));

        AccountResponse res = service.getAccount("ACC2", "SALARY");

        assertEquals("ACC2", res.accountNumber);
        assertEquals(0, res.balance.compareTo(BigDecimal.valueOf(123.45)));
    }
}

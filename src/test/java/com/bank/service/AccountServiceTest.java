package com.bank.service;

import com.bank.domain.entity.Account;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.BalanceRepository;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InvalidProductException;
import com.bank.product.ProductRule;
import com.bank.product.ProductRuleFactory;
import com.bank.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

        AccountRepository accountRepository = Mockito.mock(AccountRepository.class);
        BalanceRepository balanceRepository = Mockito.mock(BalanceRepository.class);
        ProductRuleFactory ruleFactory = Mockito.mock(ProductRuleFactory.class);
        ProductRule rule = Mockito.mock(ProductRule.class);

        AccountServiceImpl service;

        AccountServiceTest() {
                service = new AccountServiceImpl(
                                accountRepository,
                                balanceRepository,
                                ruleFactory);
        }

        @Test
        void createAccount_success() {
                CreateAccountRequest req = new CreateAccountRequest();
                req.accountNumber = "ACC1";
                req.productType = "SALARY";
                req.productCode = "SAL1";
                req.name = "John";
                req.age = 25;

                Account account = new Account();
                account.accountNumber = "ACC1";
                account.productCode = "SAL1";
                account.productType = "SALARY";
                account.name = "John";
                account.age = 25;

                com.bank.domain.entity.Balance balance = new com.bank.domain.entity.Balance();
                balance.balance = BigDecimal.ZERO;

                Mockito.when(accountRepository.findByAccountNumber("ACC1"))
                                .thenReturn(Optional.empty());
                Mockito.when(ruleFactory.getRule("SALARY"))
                                .thenReturn(rule);
                Mockito.when(rule.isValidProductCode("SAL1"))
                                .thenReturn(true);
                Mockito.doNothing().when(rule).validateAge("SAL1", 25);
                Mockito.doAnswer(invocation -> {
                                    Account acc = invocation.getArgument(0);
                                    acc.id = 1L;
                                    return null;
                                }).when(accountRepository).persist(Mockito.any(Account.class));
                Mockito.doAnswer(invocation -> {
                                    return null;
                                }).when(balanceRepository).persist(Mockito.any(com.bank.domain.entity.Balance.class));

                AccountResponse response = service.createAccount(req);

                assertEquals("ACC1", response.accountNumber);
                assertEquals(BigDecimal.ZERO, response.balance);
        }

        @Test
        void createAccount_duplicateAccount_throwsException() {
                CreateAccountRequest req = new CreateAccountRequest();
                req.accountNumber = "ACC1";

                Mockito.when(accountRepository.findByAccountNumber("ACC1"))
                                .thenReturn(Optional.of(new Account()));

                assertThrows(InvalidProductException.class,
                                () -> service.createAccount(req));
        }

        @Test
        void getAccount_notFound_throwsException() {
                Mockito.when(accountRepository.findByAccountNumber("X"))
                                .thenReturn(Optional.empty());

                assertThrows(AccountNotFoundException.class,
                                () -> service.getAccount("X", "SALARY"));
        }
        
        @Test
        void createAccount_invalidProductCode_throwsException() {
                CreateAccountRequest req = new CreateAccountRequest();
                req.accountNumber = "ACC2";
                req.productType = "SALARY";
                req.productCode = "INVALID";
                req.name = "John";
                req.age = 25;

                Mockito.when(accountRepository.findByAccountNumber("ACC2"))
                                .thenReturn(Optional.empty());
                Mockito.when(ruleFactory.getRule("SALARY"))
                                .thenReturn(rule);
                Mockito.when(rule.isValidProductCode("INVALID"))
                                .thenReturn(false);

                assertThrows(InvalidProductException.class,
                                () -> service.createAccount(req));
        }
        
        @Test
        void createAccount_ageValidationFails_throwsException() {
                CreateAccountRequest req = new CreateAccountRequest();
                req.accountNumber = "ACC3";
                req.productType = "SALARY";
                req.productCode = "SAL1";
                req.name = "John";
                req.age = 20; // Below minimum for SAL1

                Mockito.when(accountRepository.findByAccountNumber("ACC3"))
                                .thenReturn(Optional.empty());
                Mockito.when(ruleFactory.getRule("SALARY"))
                                .thenReturn(rule);
                Mockito.when(rule.isValidProductCode("SAL1"))
                                .thenReturn(true);
                Mockito.doThrow(new com.bank.exception.AgeRuleViolationException("Age too low"))
                                .when(rule).validateAge("SAL1", 20);

                assertThrows(com.bank.exception.AgeRuleViolationException.class,
                                () -> service.createAccount(req));
        }
}

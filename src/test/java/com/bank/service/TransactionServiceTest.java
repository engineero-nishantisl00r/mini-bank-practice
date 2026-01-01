package com.bank.service;

import com.bank.domain.entity.Account;
import com.bank.domain.entity.Balance;
import com.bank.domain.entity.Posting;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.BalanceRepository;
import com.bank.domain.repository.PostingRepository;
import com.bank.dto.request.TransactionRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.exception.AccountNotFoundException;
import com.bank.product.ProductRule;
import com.bank.product.ProductRuleFactory;
import com.bank.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

        AccountRepository accountRepository = Mockito.mock(AccountRepository.class);
        BalanceRepository balanceRepository = Mockito.mock(BalanceRepository.class);
        PostingRepository postingRepository = Mockito.mock(PostingRepository.class);
        ProductRuleFactory ruleFactory = Mockito.mock(ProductRuleFactory.class);
        ProductRule rule = Mockito.mock(ProductRule.class);

        TransactionServiceImpl service;

        TransactionServiceTest() {
                service = new TransactionServiceImpl(
                                accountRepository,
                                balanceRepository,
                                postingRepository,
                                ruleFactory);
        }

        @Test
        void postTransaction_credit_success() {
                // Arrange
                Account account = new Account();
                account.accountNumber = "ACC1";
                account.productType = "SALARY";
                account.productCode = "SAL1";

                Balance balance = new Balance();
                balance.account = account;
                balance.balance = BigDecimal.ZERO;

                Posting posting = new Posting();
                posting.postingNumber = "P-20251203-0001";
                posting.postingDate = LocalDate.now();
                posting.accountNumber = "ACC1";
                posting.paymentType = "CREDIT";
                posting.amount = BigDecimal.valueOf(500);

                Mockito.when(accountRepository.findByAccountNumber("ACC1"))
                                .thenReturn(Optional.of(account));
                Mockito.when(balanceRepository.findByAccount(account))
                                .thenReturn(Optional.of(balance));
                Mockito.when(ruleFactory.getRule("SALARY"))
                                .thenReturn(rule);
                Mockito.doNothing().when(rule).validateTransactionAmount("SAL1", BigDecimal.valueOf(500));
                @SuppressWarnings("unchecked")
                io.quarkus.hibernate.orm.panache.PanacheQuery<Posting> query = Mockito
                                .mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
                Mockito.when(postingRepository.find(Mockito.eq("postingDate"), Mockito.any(LocalDate.class)))
                                .thenReturn(query);
                Mockito.when(query.list())
                                .thenReturn(java.util.Collections.emptyList());
                Mockito.doAnswer(invocation -> {
                        Posting p = invocation.getArgument(0);
                        p.id = 1L;
                        return null;
                }).when(postingRepository).persist(Mockito.any(Posting.class));

                TransactionRequest request = new TransactionRequest();
                request.accountNumber = "ACC1";
                request.paymentType = "CREDIT";
                request.amount = BigDecimal.valueOf(500);
                request.date = LocalDate.now();

                // Act
                TransactionResponse response = service.postTransaction(request);

                // Assert
                assertEquals("ACC1", response.accountNumber);
                assertEquals("CREDIT", response.paymentType);
                assertEquals(BigDecimal.valueOf(500), response.amount);
                assertEquals(BigDecimal.valueOf(500), balance.balance);

                Mockito.verify(postingRepository).persist(Mockito.any(Posting.class));
                Mockito.verify(balanceRepository).persist(balance);
        }

        @Test
        void postTransaction_accountNotFound_throwsException() {
                TransactionRequest request = new TransactionRequest();
                request.accountNumber = "X";
                request.amount = BigDecimal.TEN;

                Mockito.when(accountRepository.findByAccountNumber("X"))
                                .thenReturn(Optional.empty());

                assertThrows(AccountNotFoundException.class,
                                () -> service.postTransaction(request));
        }

        @Test
        void getTransaction_notFound_throwsException() {
                Mockito.when(postingRepository.findByPostingNumber("TXN1"))
                                .thenReturn(Optional.empty());

                assertThrows(AccountNotFoundException.class,
                                () -> service.getTransaction("TXN1"));
        }

        @Test
        void getTransaction_success_returnsTransactionResponse() {
                Posting posting = new Posting();
                posting.postingNumber = "P-20251203-0001";
                posting.postingDate = LocalDate.of(2025, 12, 3);
                posting.accountNumber = "ACC10";
                posting.paymentType = "DEBIT";
                posting.amount = BigDecimal.valueOf(150);

                Mockito.when(postingRepository.findByPostingNumber("P-20251203-0001"))
                                .thenReturn(Optional.of(posting));

                TransactionResponse resp = service.getTransaction("P-20251203-0001");

                assertEquals("P-20251203-0001", resp.postingNumber);
                assertEquals("ACC10", resp.accountNumber);
                assertEquals("DEBIT", resp.paymentType);
                assertEquals(BigDecimal.valueOf(150), resp.amount);
                assertEquals(LocalDate.of(2025, 12, 3), resp.date);
        }

        @Test
        void postTransaction_debit_success() {
                Account account = new Account();
                account.accountNumber = "ACC2";
                account.productType = "SALARY";
                account.productCode = "SAL1";

                Balance balance = new Balance();
                balance.account = account;
                balance.balance = BigDecimal.valueOf(1000);

                Mockito.when(accountRepository.findByAccountNumber("ACC2"))
                                .thenReturn(Optional.of(account));
                Mockito.when(balanceRepository.findByAccount(account))
                                .thenReturn(Optional.of(balance));
                Mockito.when(ruleFactory.getRule("SALARY"))
                                .thenReturn(rule);
                Mockito.doNothing().when(rule).validateTransactionAmount("SAL1", BigDecimal.valueOf(150));
                @SuppressWarnings("unchecked")
                io.quarkus.hibernate.orm.panache.PanacheQuery<Posting> query = Mockito
                                .mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
                Mockito.when(postingRepository.find(Mockito.eq("postingDate"), Mockito.any(LocalDate.class)))
                                .thenReturn(query);
                Mockito.when(query.list())
                                .thenReturn(java.util.Collections.emptyList());
                Mockito.doAnswer(invocation -> {
                        Posting p = invocation.getArgument(0);
                        p.id = 1L;
                        return null;
                }).when(postingRepository).persist(Mockito.any(Posting.class));

                TransactionRequest request = new TransactionRequest();
                request.accountNumber = "ACC2";
                request.paymentType = "DEBIT";
                request.amount = BigDecimal.valueOf(150);
                request.date = LocalDate.now();

                TransactionResponse response = service.postTransaction(request);

                assertEquals("ACC2", response.accountNumber);
                assertEquals("DEBIT", response.paymentType);
                assertEquals(BigDecimal.valueOf(150), response.amount);
                assertEquals(BigDecimal.valueOf(850), balance.balance);

                Mockito.verify(postingRepository).persist(Mockito.any(Posting.class));
                Mockito.verify(balanceRepository).persist(balance);
        }

        @Test
        void postTransaction_insufficientBalance_throwsException() {
                Account account = new Account();
                account.accountNumber = "ACC3";
                account.productType = "SALARY";
                account.productCode = "SAL1";

                Balance balance = new Balance();
                balance.account = account;
                balance.balance = BigDecimal.valueOf(50);

                Mockito.when(accountRepository.findByAccountNumber("ACC3"))
                                .thenReturn(Optional.of(account));
                Mockito.when(balanceRepository.findByAccount(account))
                                .thenReturn(Optional.of(balance));
                Mockito.when(ruleFactory.getRule("SALARY"))
                                .thenReturn(rule);
                Mockito.doNothing().when(rule).validateTransactionAmount("SAL1", BigDecimal.valueOf(100));

                TransactionRequest request = new TransactionRequest();
                request.accountNumber = "ACC3";
                request.paymentType = "DEBIT";
                request.amount = BigDecimal.valueOf(100);

                assertThrows(com.bank.exception.InsufficientBalanceException.class,
                                () -> service.postTransaction(request));
        }

        @Test
        void postTransaction_amountValidationFails_throwsException() {
                Account account = new Account();
                account.accountNumber = "ACC4";
                account.productType = "SALARY";
                account.productCode = "SAL1";

                Balance balance = new Balance();
                balance.account = account;
                balance.balance = BigDecimal.valueOf(1000);

                Mockito.when(accountRepository.findByAccountNumber("ACC4"))
                                .thenReturn(Optional.of(account));
                Mockito.when(balanceRepository.findByAccount(account))
                                .thenReturn(Optional.of(balance));
                Mockito.when(ruleFactory.getRule("SALARY"))
                                .thenReturn(rule);
                Mockito.doThrow(new com.bank.exception.AmountRuleViolationException("Amount too low"))
                                .when(rule).validateTransactionAmount("SAL1", BigDecimal.valueOf(50));

                TransactionRequest request = new TransactionRequest();
                request.accountNumber = "ACC4";
                request.paymentType = "CREDIT";
                request.amount = BigDecimal.valueOf(50);

                assertThrows(com.bank.exception.AmountRuleViolationException.class,
                                () -> service.postTransaction(request));
        }

        @Test
        void postTransaction_upi_throwsInvalidProductException() {
                Account account = new Account();
                account.accountNumber = "ACC5";
                account.productType = "SALARY";
                account.productCode = "SAL1";

                Balance balance = new Balance();
                balance.account = account;
                balance.balance = BigDecimal.valueOf(1000);

                Mockito.when(accountRepository.findByAccountNumber("ACC5"))
                                .thenReturn(Optional.of(account));
                Mockito.when(balanceRepository.findByAccount(account))
                                .thenReturn(Optional.of(balance));

                TransactionRequest request = new TransactionRequest();
                request.accountNumber = "ACC5";
                request.paymentType = "UPI";
                request.amount = BigDecimal.valueOf(100);

                assertThrows(com.bank.exception.InvalidProductException.class,
                                () -> service.postTransaction(request));
        }

        @Test
        void postTransaction_all_throwsInvalidProductException() {
                Account account = new Account();
                account.accountNumber = "ACC6";
                account.productType = "SALARY";
                account.productCode = "SAL1";

                Balance balance = new Balance();
                balance.account = account;
                balance.balance = BigDecimal.valueOf(1000);

                Mockito.when(accountRepository.findByAccountNumber("ACC6"))
                                .thenReturn(Optional.of(account));
                Mockito.when(balanceRepository.findByAccount(account))
                                .thenReturn(Optional.of(balance));

                TransactionRequest request = new TransactionRequest();
                request.accountNumber = "ACC6";
                request.paymentType = "ALL";
                request.amount = BigDecimal.valueOf(100);

                assertThrows(com.bank.exception.InvalidProductException.class,
                                () -> service.postTransaction(request));
        }

        @Test
        void postTransaction_generatesPostingNumber_and_incrementsSequence() {
                Account account = new Account();
                account.accountNumber = "ACC7";
                account.productType = "SALARY";
                account.productCode = "SAL1";

                Balance balance = new Balance();
                balance.account = account;
                balance.balance = BigDecimal.valueOf(1000);

                Mockito.when(accountRepository.findByAccountNumber("ACC7"))
                                .thenReturn(Optional.of(account));
                Mockito.when(balanceRepository.findByAccount(account))
                                .thenReturn(Optional.of(balance));
                Mockito.when(ruleFactory.getRule("SALARY"))
                                .thenReturn(rule);
                Mockito.doNothing().when(rule).validateTransactionAmount("SAL1", BigDecimal.valueOf(100));

                // Simulate existing posting for the date so next sequence becomes 0002
                @SuppressWarnings("unchecked")
                io.quarkus.hibernate.orm.panache.PanacheQuery<Posting> query = Mockito
                                .mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
                Posting existing = new Posting();
                existing.postingNumber = "P-20251203-0001";
                LocalDate date = LocalDate.of(2025, 12, 3);
                Mockito.when(postingRepository.find(Mockito.eq("postingDate"), Mockito.eq(date)))
                                .thenReturn(query);
                Mockito.when(query.list())
                                .thenReturn(java.util.List.of(existing));

                // Capture persisted posting
                final Posting[] captured = new Posting[1];
                Mockito.doAnswer(invocation -> {
                        Posting p = invocation.getArgument(0);
                        p.id = 99L;
                        captured[0] = p;
                        return null;
                }).when(postingRepository).persist(Mockito.any(Posting.class));

                TransactionRequest request = new TransactionRequest();
                request.accountNumber = "ACC7";
                request.paymentType = "CREDIT";
                request.amount = BigDecimal.valueOf(100);
                request.date = date;

                TransactionResponse response = service.postTransaction(request);

                // Expect next sequence 0002
                assertEquals("P-20251203-0002", response.postingNumber);
                assertNotNull(captured[0]);
                assertEquals("P-20251203-0002", captured[0].postingNumber);
        }
}

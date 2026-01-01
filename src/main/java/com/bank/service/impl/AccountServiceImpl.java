package com.bank.service.impl;

import com.bank.domain.entity.Account;
import com.bank.domain.entity.Balance;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.BalanceRepository;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InvalidProductException;
import com.bank.product.ProductRule;
import com.bank.product.ProductRuleFactory;
import com.bank.service.AccountService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Inject
    AccountRepository accountRepository;

    @Inject
    BalanceRepository balanceRepository;

    @Inject
    ProductRuleFactory productRuleFactory;

    // 🔹 Default constructor for CDI
    public AccountServiceImpl() {
    }

    // 🔹 Constructor for unit tests (Mockito)
    public AccountServiceImpl(AccountRepository accountRepository,
            BalanceRepository balanceRepository,
            ProductRuleFactory productRuleFactory) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.productRuleFactory = productRuleFactory;
    }

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {

        // 1️⃣ Check duplicate account
        accountRepository.findByAccountNumber(request.accountNumber)
                .ifPresent(acc -> {
                    throw new InvalidProductException(
                            "Account already exists: " + request.accountNumber);
                });

        // 2️⃣ Get product rule and validate productCode exists
        ProductRule rule = productRuleFactory.getRule(request.productType);
        if (!rule.isValidProductCode(request.productCode)) {
            throw new InvalidProductException(
                    "Product code " + request.productCode + " not found for product type " + request.productType);
        }

        // 3️⃣ Apply product age rules
        rule.validateAge(request.productCode, request.age);

        // 4️⃣ Create Account entity
        Account account = new Account();
        account.accountNumber = request.accountNumber;
        account.productCode = request.productCode;
        account.productType = request.productType;
        account.name = request.name;
        account.age = request.age;

        accountRepository.persist(account);

        // 5️⃣ Create Balance entity
        Balance balance = new Balance();
        balance.account = account;
        balance.balance = BigDecimal.ZERO;

        balanceRepository.persist(balance);

        // 6️⃣ Return response
        return toAccountResponse(account, balance.balance);
    }

    @Override
    public AccountResponse getAccount(String accountNumber, String productType) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        Balance balance = balanceRepository.findByAccount(account)
                .orElseThrow(() -> new AccountNotFoundException("Balance not found for account: " + accountNumber));

        return toAccountResponse(account, balance.balance);
    }

    // 🔹 Mapper method
    private AccountResponse toAccountResponse(Account account, BigDecimal balance) {
        AccountResponse response = new AccountResponse();
        response.accountNumber = account.accountNumber;
        response.productCode = account.productCode;
        response.productType = account.productType;
        response.name = account.name;
        response.age = account.age;
        response.balance = balance;
        return response;
    }
}

package com.bank.service.impl;

import com.bank.domain.entity.Account;
import com.bank.domain.entity.Balance;
import com.bank.domain.entity.Posting;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.BalanceRepository;
import com.bank.domain.repository.PostingRepository;
import com.bank.dto.request.TransactionRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidProductException;
import com.bank.product.ProductRule;
import com.bank.product.ProductRuleFactory;
import com.bank.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class TransactionServiceImpl implements TransactionService {

    @Inject
    AccountRepository accountRepository;

    @Inject
    BalanceRepository balanceRepository;

    @Inject
    PostingRepository postingRepository;

    @Inject
    ProductRuleFactory productRuleFactory;

    // 🔹 Default constructor for CDI
    public TransactionServiceImpl() {
    }

    // 🔹 Constructor for unit tests (Mockito)
    public TransactionServiceImpl(AccountRepository accountRepository,
            BalanceRepository balanceRepository,
            PostingRepository postingRepository,
            ProductRuleFactory productRuleFactory) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.postingRepository = postingRepository;
        this.productRuleFactory = productRuleFactory;
    }

    @Override
    @Transactional
    public TransactionResponse postTransaction(TransactionRequest request) {

        // 1️⃣ Find account
        Account account = accountRepository.findByAccountNumber(request.accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.accountNumber));

        // 2️⃣ Load balance
        Balance balance = balanceRepository.findByAccount(account)
                .orElseThrow(
                        () -> new AccountNotFoundException("Balance not found for account: " + request.accountNumber));

        // 3️⃣ Validate payment type early to fail fast for unsupported types
        if (!"DEBIT".equalsIgnoreCase(request.paymentType) && !"CREDIT".equalsIgnoreCase(request.paymentType)) {
            throw new InvalidProductException("Invalid payment type: " + request.paymentType);
        }

        // 4️⃣ Apply product transaction rules
        ProductRule rule = productRuleFactory.getRule(account.productType);
        rule.validateTransactionAmount(account.productCode, request.amount);

        // 5️⃣ Apply debit / credit
        if ("DEBIT".equalsIgnoreCase(request.paymentType)) {
            if (balance.balance.compareTo(request.amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
            balance.balance = balance.balance.subtract(request.amount);

        } else if ("CREDIT".equalsIgnoreCase(request.paymentType)) {
            balance.balance = balance.balance.add(request.amount);

        } else {
            throw new InvalidProductException("Invalid payment type: " + request.paymentType);
        }

        // 5️⃣ Persist updated balance
        balanceRepository.persist(balance);

        // 6️⃣ Generate posting number and persist posting
        LocalDate postingDate = request.date != null ? request.date : LocalDate.now();
        String postingNumber = generatePostingNumber(postingDate);

        Posting posting = new Posting();
        posting.postingNumber = postingNumber;
        posting.postingDate = postingDate;
        posting.account = account;
        posting.accountNumber = account.accountNumber;
        posting.paymentType = request.paymentType;
        posting.amount = request.amount;

        postingRepository.persist(posting);

        // 7️⃣ Return response
        return toTransactionResponse(posting);
    }

    @Override
    public TransactionResponse getTransaction(String postingNumber) {

        Posting posting = postingRepository.findByPostingNumber(postingNumber)
                .orElseThrow(() -> new AccountNotFoundException("Transaction not found: " + postingNumber));

        return toTransactionResponse(posting);
    }

    // 🔹 Generate posting number in format P-YYYYMMDD-XXXX
    private String generatePostingNumber(LocalDate date) {
        String datePrefix = "P-" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";

        // Find the highest sequence number for this date
        List<Posting> postingsForDate = postingRepository.find("postingDate", date).list();

        int maxSequence = 0;
        for (Posting p : postingsForDate) {
            if (p.postingNumber != null && p.postingNumber.startsWith(datePrefix)) {
                try {
                    String sequenceStr = p.postingNumber.substring(datePrefix.length());
                    int sequence = Integer.parseInt(sequenceStr);
                    if (sequence > maxSequence) {
                        maxSequence = sequence;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }

        // Increment and format with 4 digits
        int nextSequence = maxSequence + 1;
        return datePrefix + String.format("%04d", nextSequence);
    }

    // 🔹 Mapper method
    private TransactionResponse toTransactionResponse(Posting posting) {
        TransactionResponse response = new TransactionResponse();
        response.postingNumber = posting.postingNumber;
        response.accountNumber = posting.accountNumber;
        response.paymentType = posting.paymentType;
        response.amount = posting.amount;
        response.date = posting.postingDate;
        return response;
    }
}

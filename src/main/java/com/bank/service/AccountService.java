package com.bank.service;

import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.response.AccountResponse;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    AccountResponse getAccount(String accountNumber, String productType);
}

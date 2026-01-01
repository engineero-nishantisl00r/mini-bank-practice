package com.bank.service;

import com.bank.dto.request.TransactionRequest;
import com.bank.dto.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse postTransaction(TransactionRequest request);

    TransactionResponse getTransaction(String postingNumber);
}

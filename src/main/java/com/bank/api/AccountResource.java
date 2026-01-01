package com.bank.api;

import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.service.AccountService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    // 1️⃣ Create Account
    @POST
    public AccountResponse createAccount(CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    // 2️⃣ Retrieve Account
    @GET
    public AccountResponse getAccount(
            @QueryParam("accountNumber") String accountNumber,
            @QueryParam("productType") String productType) {

        return accountService.getAccount(accountNumber, productType);
    }
}

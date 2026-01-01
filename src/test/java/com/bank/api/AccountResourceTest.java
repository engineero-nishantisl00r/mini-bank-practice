package com.bank.api;

import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountResourceTest {

    private AccountService accountService;
    private AccountResource resource;

    @BeforeEach
    void setup() {
        accountService = mock(AccountService.class);
        resource = new AccountResource();
        resource.accountService = accountService;
    }

    @Test
    void createAndGetAccount_directInvocation() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.accountNumber = "ACC_U1";
        req.productCode = "SAL1";
        req.productType = "SALARY";
        req.name = "UnitUser";
        req.age = 25;

        AccountResponse resp = new AccountResponse();
        resp.accountNumber = req.accountNumber;
        resp.name = req.name;
        resp.productType = req.productType;

        when(accountService.createAccount(req)).thenReturn(resp);
        when(accountService.getAccount(req.accountNumber, req.productType)).thenReturn(resp);

        AccountResponse created = resource.createAccount(req);
        assertEquals("ACC_U1", created.accountNumber);

        AccountResponse fetched = resource.getAccount(req.accountNumber, req.productType);
        assertEquals("UnitUser", fetched.name);
    }
}

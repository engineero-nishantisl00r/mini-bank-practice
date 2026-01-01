package com.bank.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class TransactionResourceIT {

    @Test
    void postCredit_and_getTransaction_and_getAccount() {
        String acct = "HTTP-TXN-1";
        String create = "{\n" +
                "  \"productCode\": \"SAL1\",\n" +
                "  \"productType\": \"SALARY\",\n" +
                "  \"name\": \"TxnUser\",\n" +
                "  \"age\": 31,\n" +
                "  \"accountNumber\": \"" + acct + "\"\n" +
                "}";

        given().contentType("application/json").body(create).post("/api/v1/accounts").then().statusCode(200);

        String txnBody = "{\n" +
                "  \"accountNumber\": \"" + acct + "\",\n" +
                "  \"paymentType\": \"CREDIT\",\n" +
                "  \"amount\": 250.00,\n" +
                "  \"date\": \"2025-12-03\"\n" +
                "}";

        String postingNumber = given().contentType("application/json").body(txnBody).post("/api/v1/transactions")
                .then().statusCode(200).body("accountNumber", equalTo(acct))
                .extract().path("postingNumber");

        given().get("/api/v1/transactions/" + postingNumber).then().statusCode(200).body("postingNumber",
                equalTo(postingNumber));

        given().queryParam("accountNumber", acct).queryParam("productType", "SALARY").get("/api/v1/accounts")
                .then().statusCode(200).body("balance", equalTo(250.00f));
    }

    @Test
    void postDebit_and_insufficientFunds_and_invalidPaymentType() {
        String acct = "HTTP-TXN-2";
        String create = "{\n" +
                "  \"productCode\": \"SAL1\",\n" +
                "  \"productType\": \"SALARY\",\n" +
                "  \"name\": \"TxnUser2\",\n" +
                "  \"age\": 31,\n" +
                "  \"accountNumber\": \"" + acct + "\"\n" +
                "}";

        given().contentType("application/json").body(create).post("/api/v1/accounts").then().statusCode(200);

        // debit without credit -> insufficient
        String debit = "{\n" +
                "  \"accountNumber\": \"" + acct + "\",\n" +
                "  \"paymentType\": \"DEBIT\",\n" +
                "  \"amount\": 100.00,\n" +
                "  \"date\": \"2025-12-03\"\n" +
                "}";

        given().contentType("application/json").body(debit).post("/api/v1/transactions")
                .then().statusCode(400).body("errorCode", equalTo("INSUFFICIENT_BALANCE"));

        // invalid payment type
        String upi = "{\n" +
                "  \"accountNumber\": \"" + acct + "\",\n" +
                "  \"paymentType\": \"UPI\",\n" +
                "  \"amount\": 10.00,\n" +
                "  \"date\": \"2025-12-03\"\n" +
                "}";

        given().contentType("application/json").body(upi).post("/api/v1/transactions")
                .then().statusCode(400).body("errorCode", equalTo("INVALID_PRODUCT"));
    }
}

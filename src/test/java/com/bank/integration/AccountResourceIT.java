package com.bank.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class AccountResourceIT {

    @Test
    void createAccount_and_getAccount_success() {
        String body = "{\n" +
                "  \"productCode\": \"SAL1\",\n" +
                "  \"productType\": \"SALARY\",\n" +
                "  \"name\": \"HTTPUser\",\n" +
                "  \"age\": 30,\n" +
                "  \"accountNumber\": \"HTTP-ACC-1\"\n" +
                "}";

        given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/api/v1/accounts")
                .then()
                .statusCode(200)
                .body("accountNumber", equalTo("HTTP-ACC-1"))
                .body("balance", equalTo(0));

        given()
                .queryParam("accountNumber", "HTTP-ACC-1")
                .queryParam("productType", "SALARY")
                .when()
                .get("/api/v1/accounts")
                .then()
                .statusCode(200)
                .body("accountNumber", equalTo("HTTP-ACC-1"))
                .body("balance", equalTo(0));
    }

    @Test
    void createAccount_duplicate_returnsBadRequest() {
        String body = "{\n" +
                "  \"productCode\": \"SAL1\",\n" +
                "  \"productType\": \"SALARY\",\n" +
                "  \"name\": \"HTTPUser\",\n" +
                "  \"age\": 30,\n" +
                "  \"accountNumber\": \"HTTP-ACC-2\"\n" +
                "}";

        given().contentType("application/json").body(body).post("/api/v1/accounts").then().statusCode(200);

        given().contentType("application/json").body(body).post("/api/v1/accounts")
                .then().statusCode(400)
                .body("errorCode", equalTo("INVALID_PRODUCT"));
    }

    @Test
    void createAccount_invalidProduct_returnsBadRequest() {
        String body = "{\n" +
                "  \"productCode\": \"INVALID\",\n" +
                "  \"productType\": \"SALARY\",\n" +
                "  \"name\": \"HTTPUser\",\n" +
                "  \"age\": 30,\n" +
                "  \"accountNumber\": \"HTTP-ACC-3\"\n" +
                "}";

        given().contentType("application/json").body(body).post("/api/v1/accounts")
                .then().statusCode(400)
                .body("errorCode", equalTo("INVALID_PRODUCT"));
    }

    @Test
    void createAccount_student_ageValidation_returnsBadRequest() {
        String body = "{\n" +
                "  \"productCode\": \"STU1\",\n" +
                "  \"productType\": \"STUDENT\",\n" +
                "  \"name\": \"YoungUser\",\n" +
                "  \"age\": 30,\n" +
                "  \"accountNumber\": \"HTTP-ACC-4\"\n" +
                "}";

        given().contentType("application/json").body(body).post("/api/v1/accounts")
                .then().statusCode(400)
                .body("errorCode", equalTo("AGE_RULE_VIOLATION"));
    }
}

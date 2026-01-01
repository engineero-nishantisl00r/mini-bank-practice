package com.bank.api;

import com.bank.dto.request.TransactionRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.service.TransactionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    // 3️⃣ Post Transaction
    @POST
    public TransactionResponse postTransaction(TransactionRequest request) {
        return transactionService.postTransaction(request);
    }

    // 4️⃣ Retrieve Transaction
    @GET
    @Path("/{postingNumber}")
    public TransactionResponse getTransaction(@PathParam("postingNumber") String postingNumber) {
        return transactionService.getTransaction(postingNumber);
    }
}

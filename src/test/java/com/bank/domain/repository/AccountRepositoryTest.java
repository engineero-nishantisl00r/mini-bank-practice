package com.bank.domain.repository;

import com.bank.domain.entity.Account;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountRepositoryTest {

    @Test
    void findByAccountNumber_returnsOptionalWhenFound() {
        AccountRepository repo = Mockito.spy(new AccountRepository());

        PanacheQuery<Account> query = Mockito.mock(PanacheQuery.class);
        Account acc = new Account();
        acc.accountNumber = "ACC100";

        Mockito.when(query.firstResultOptional()).thenReturn(Optional.of(acc));
        Mockito.doReturn(query).when(repo).find(Mockito.eq("accountNumber"), Mockito.eq("ACC100"));

        Optional<Account> res = repo.findByAccountNumber("ACC100");
        assertTrue(res.isPresent());
        assertEquals("ACC100", res.get().accountNumber);
    }

    @Test
    void findByAccountNumber_returnsEmptyWhenNotFound() {
        AccountRepository repo = Mockito.spy(new AccountRepository());

        PanacheQuery<Account> query = Mockito.mock(PanacheQuery.class);
        Mockito.when(query.firstResultOptional()).thenReturn(Optional.empty());
        Mockito.doReturn(query).when(repo).find(Mockito.eq("accountNumber"), Mockito.eq("X"));

        Optional<Account> res = repo.findByAccountNumber("X");
        assertTrue(res.isEmpty());
    }
}

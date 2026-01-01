package com.bank.domain.repository;

import com.bank.domain.entity.Account;
import com.bank.domain.entity.Balance;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BalanceRepositoryTest {

    @Test
    void findByAccount_returnsOptionalWhenFound() {
        BalanceRepository repo = Mockito.spy(new BalanceRepository());

        PanacheQuery<Balance> query = Mockito.mock(PanacheQuery.class);
        Balance bal = new Balance();
        Account acc = new Account();
        acc.accountNumber = "ACC200";
        bal.account = acc;
        bal.balance = java.math.BigDecimal.valueOf(10);

        Mockito.when(query.firstResultOptional()).thenReturn(Optional.of(bal));
        Mockito.doReturn(query).when(repo).find(Mockito.eq("account"), Mockito.eq(acc));

        Optional<Balance> res = repo.findByAccount(acc);
        assertTrue(res.isPresent());
        assertEquals("ACC200", res.get().account.accountNumber);
    }

    @Test
    void findByAccount_returnsEmptyWhenNotFound() {
        BalanceRepository repo = Mockito.spy(new BalanceRepository());

        PanacheQuery<Balance> query = Mockito.mock(PanacheQuery.class);
        Account acc = new Account();
        Mockito.when(query.firstResultOptional()).thenReturn(Optional.empty());
        Mockito.doReturn(query).when(repo).find(Mockito.eq("account"), Mockito.eq(acc));

        Optional<Balance> res = repo.findByAccount(acc);
        assertTrue(res.isEmpty());
    }
}

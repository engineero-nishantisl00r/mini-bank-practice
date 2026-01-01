package com.bank.domain.repository;

import com.bank.domain.entity.Account;
import com.bank.domain.entity.Balance;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class BalanceRepository implements PanacheRepository<Balance> {

    public Optional<Balance> findByAccount(Account account) {
        return find("account", account).firstResultOptional();
    }
}

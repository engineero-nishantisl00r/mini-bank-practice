package com.bank.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "balance")
public class Balance extends PanacheEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "account_id", unique = true)
    public Account account;

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal balance = BigDecimal.ZERO;
}

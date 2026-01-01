package com.bank.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "postings")
public class Posting extends PanacheEntity {

    @Column(name = "posting_number", nullable = false, unique = true)
    public String postingNumber;

    @Column(name = "posting_date", nullable = false)
    public LocalDate postingDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    public Account account;

    @Column(name = "account_number", nullable = false)
    public String accountNumber;

    @Column(name = "payment_type", nullable = false)
    public String paymentType; // CREDIT / DEBIT

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal amount;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}

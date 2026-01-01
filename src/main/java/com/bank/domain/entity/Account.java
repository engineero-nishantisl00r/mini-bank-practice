package com.bank.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account extends PanacheEntity {

    @Column(name = "account_number", nullable = false, unique = true)
    public String accountNumber;

    @Column(name = "product_code", nullable = false)
    public String productCode;

    @Column(name = "product_type", nullable = false)
    public String productType; // SALARY or STUDENT

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public int age;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}

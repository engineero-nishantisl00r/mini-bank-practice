package com.bank.config;

import io.smallrye.config.ConfigMapping;
import java.util.List;

@ConfigMapping(prefix = "salary-products")
public interface SalaryProductConfig {

    List<SalaryRule> rules();

    interface SalaryRule {
        String code();

        int minAge();

        int minTransactionAmount();
    }
}

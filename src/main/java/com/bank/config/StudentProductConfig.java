package com.bank.config;

import io.smallrye.config.ConfigMapping;
import java.util.List;

@ConfigMapping(prefix = "student-products")
public interface StudentProductConfig {

    List<StudentRule> rules();

    interface StudentRule {
        String code();

        int maxAge();

        int maxTransactionAmount();
    }
}

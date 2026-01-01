package com.bank.product;

import com.bank.exception.InvalidProductException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductRuleFactoryTest {

    @Test
    void getRule_salaryAndStudentAndInvalid() {
        ProductRuleFactory factory = new ProductRuleFactory();

        SalaryProductRule salary = new SalaryProductRule();
        StudentProductRule student = new StudentProductRule();

        factory.salaryProductRule = salary;
        factory.studentProductRule = student;

        assertSame(salary, factory.getRule("SALARY"));
        assertSame(student, factory.getRule("student"));

        InvalidProductException ex = assertThrows(InvalidProductException.class, () -> factory.getRule("UNKNOWN"));
        assertTrue(ex.getMessage().contains("Unknown product type"));

        // null or blank productType should result in InvalidProductException
        InvalidProductException ex2 = assertThrows(InvalidProductException.class, () -> factory.getRule(null));
        assertTrue(ex2.getMessage().contains("productType is required"));

        InvalidProductException ex3 = assertThrows(InvalidProductException.class, () -> factory.getRule("   "));
        assertTrue(ex3.getMessage().contains("productType is required"));
    }

}

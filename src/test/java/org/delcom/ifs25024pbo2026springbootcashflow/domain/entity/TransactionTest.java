package org.delcom.ifs25024pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionTest {

    @Test
    void constructorSetsFieldsAndCreatedAt() {
        Transaction transaction = new Transaction(1, "Gaji", 5000000, TransactionType.INCOME);

        assertEquals(1, transaction.getId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(5000000, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void constructorKeepsGivenCreatedAt() {
        LocalDateTime time = LocalDateTime.of(2026, 11, 7, 10, 30);
        Transaction transaction = new Transaction(2, "Makan", 25000, TransactionType.EXPENSE, time);

        assertEquals(time, transaction.getCreatedAt());
    }

    @Test
    void changeMethodsUpdateData() {
        Transaction transaction = new Transaction(1, "Gaji", 100, TransactionType.INCOME);

        transaction.changeDescription("Bonus");
        transaction.changeAmount(250);

        assertEquals("Bonus", transaction.getDescription());
        assertEquals(250, transaction.getAmount());
    }

    @Test
    void signedAmountIsPositiveForIncomeAndNegativeForExpense() {
        assertEquals(100, new Transaction(1, "A", 100, TransactionType.INCOME).signedAmount());
        assertEquals(-100, new Transaction(2, "B", 100, TransactionType.EXPENSE).signedAmount());
    }
}

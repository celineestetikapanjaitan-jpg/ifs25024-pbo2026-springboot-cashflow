package org.delcom.ifs25024pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private final Transaction income = new Transaction(1, "Gaji", 500, TransactionType.INCOME);
    private final Transaction smallExpense = new Transaction(2, "Kopi", 10, TransactionType.EXPENSE);
    private final Transaction bigExpense = new Transaction(3, "Sewa", 900, TransactionType.EXPENSE);

    private List<Integer> sortedIds(SortOption option) {
        return List.of(income, smallExpense, bigExpense).stream()
                .sorted(option.comparator())
                .map(Transaction::getId)
                .toList();
    }

    @Test
    void amountAscendingOrdersSmallestFirst() {
        assertEquals(List.of(2, 1, 3), sortedIds(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescendingOrdersLargestFirst() {
        assertEquals(List.of(3, 1, 2), sortedIds(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirstPutsIncomeBeforeExpense() {
        assertEquals(1, sortedIds(SortOption.INCOME_FIRST).get(0));
    }

    @Test
    void expenseFirstPutsExpenseBeforeIncome() {
        assertEquals(1, sortedIds(SortOption.EXPENSE_FIRST).get(2));
    }

    @Test
    void hasFourOptions() {
        assertEquals(4, SortOption.values().length);
    }
}

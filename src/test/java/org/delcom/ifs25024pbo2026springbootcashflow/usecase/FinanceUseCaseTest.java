package org.delcom.ifs25024pbo2026springbootcashflow.usecase;

import org.delcom.ifs25024pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void balanceIsZeroWhenEmpty() {
        assertEquals(0.0, useCase.getBalance());
        assertEquals(0.0, useCase.getTotalIncome());
        assertEquals(0.0, useCase.getTotalExpense());
    }

    @Test
    void incomeIncreasesAndExpenseDecreasesBalance() throws Exception {
        useCase.addIncome("Gaji", 5000000);
        useCase.addExpense("Makan", 25000);

        assertEquals(5000000, useCase.getTotalIncome());
        assertEquals(25000, useCase.getTotalExpense());
        assertEquals(4975000, useCase.getBalance());
    }

    @Test
    void addTrimsDescription() throws Exception {
        Transaction saved = useCase.addIncome("  Gaji  ", 10);
        assertEquals("Gaji", saved.getDescription());
    }

    @Test
    void addRejectsBlankOrNullDescription() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(null, 10));
        assertThrows(ValidationException.class, () -> useCase.addIncome("   ", 10));
        assertThrows(ValidationException.class, () -> useCase.addExpense("", 10));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void addRejectsNonPositiveOrNonNumericAmount() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("A", 0));
        assertThrows(ValidationException.class, () -> useCase.addIncome("A", -5));
        assertThrows(ValidationException.class, () -> useCase.addExpense("A", Double.NaN));
        assertThrows(ValidationException.class, () -> useCase.addExpense("A", Double.POSITIVE_INFINITY));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void removeUpdatesBalanceAndThrowsWhenMissing() throws Exception {
        Transaction income = useCase.addIncome("Gaji", 100);
        useCase.addExpense("Makan", 30);

        useCase.removeTransaction(income.getId());

        assertEquals(-30, useCase.getBalance());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> useCase.removeTransaction(income.getId()));
        assertEquals(income.getId(), ex.getId());
    }

    @Test
    void searchIsCaseInsensitiveAndDoesNotChangeData() throws Exception {
        useCase.addIncome("Gaji Bulanan", 100);
        useCase.addExpense("Makan siang", 30);

        List<Transaction> result = useCase.searchTransactions("GAJI");

        assertEquals(1, result.size());
        assertEquals("Gaji Bulanan", result.get(0).getDescription());
        assertEquals(0, useCase.searchTransactions("tidakada").size());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void sortUsesSortOptionAndKeepsOriginalOrder() throws Exception {
        useCase.addIncome("Gaji", 500);
        useCase.addExpense("Kopi", 10);
        useCase.addExpense("Sewa", 900);

        assertEquals("Kopi", useCase.sortTransactions(SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals("Sewa", useCase.sortTransactions(SortOption.AMOUNT_DESC).get(0).getDescription());
        assertEquals("Gaji", useCase.sortTransactions(SortOption.INCOME_FIRST).get(0).getDescription());
        assertEquals("Gaji", useCase.sortTransactions(SortOption.EXPENSE_FIRST).get(2).getDescription());
        assertEquals("Gaji", useCase.getAllTransactions().get(0).getDescription());
    }
}

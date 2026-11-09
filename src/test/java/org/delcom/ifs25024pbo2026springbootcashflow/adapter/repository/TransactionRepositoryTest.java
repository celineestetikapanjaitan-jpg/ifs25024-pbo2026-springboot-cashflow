package org.delcom.ifs25024pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveIncomeAndExpenseAssignIncrementingIds() {
        Transaction income = repository.saveIncome("Gaji", 100);
        Transaction expense = repository.saveExpense("Makan", 20);

        assertEquals(1, income.getId());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(2, expense.getId());
        assertEquals(TransactionType.EXPENSE, expense.getType());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.saveIncome("Gaji", 100);

        List<Transaction> copy = repository.findAll();
        copy.clear();

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByIdReturnsPresentOrEmpty() {
        Transaction saved = repository.saveIncome("Gaji", 100);

        assertTrue(repository.findById(saved.getId()).isPresent());
        assertFalse(repository.findById(99).isPresent());
    }

    @Test
    void deleteByIdReturnsTrueOnlyWhenRemoved() {
        Transaction saved = repository.saveIncome("Gaji", 100);

        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findByFiltersWithPredicate() {
        repository.saveIncome("Gaji", 100);
        repository.saveExpense("Makan", 20);

        List<Transaction> result = repository.findBy(t -> t.getType() == TransactionType.EXPENSE);

        assertEquals(1, result.size());
        assertEquals("Makan", result.get(0).getDescription());
    }

    @Test
    void updateKeepsMutatedEntity() {
        Transaction saved = repository.saveIncome("Gaji", 100);
        saved.changeDescription("Gaji Bulanan");

        repository.update(saved);

        assertEquals("Gaji Bulanan", repository.findById(saved.getId()).orElseThrow().getDescription());
    }
}

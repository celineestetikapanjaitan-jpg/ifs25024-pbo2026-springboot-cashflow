package org.delcom.ifs25024pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.repository.ITransactionRepository;

/**
 * Implementasi repository transaksi memakai basis generic {@link InMemoryRepository}.
 * Data disimpan di memori dan hilang saat aplikasi di-restart.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    /** Penghitung ID otomatis, bertambah setiap transaksi baru disimpan. */
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction saveIncome(String description, double amount) {
        return save(new Transaction(++idCounter, description, amount, TransactionType.INCOME));
    }

    @Override
    public Transaction saveExpense(String description, double amount) {
        return save(new Transaction(++idCounter, description, amount, TransactionType.EXPENSE));
    }
}

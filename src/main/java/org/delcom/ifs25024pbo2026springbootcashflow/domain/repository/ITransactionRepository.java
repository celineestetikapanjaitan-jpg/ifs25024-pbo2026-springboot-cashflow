package org.delcom.ifs25024pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;

/**
 * Port spesifik transaksi yang memperluas generic {@link IRepository}.
 * ID dibuat otomatis oleh implementasi, sehingga penyimpanan memakai method khusus.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan pemasukan baru dan mengembalikannya lengkap dengan ID. */
    Transaction saveIncome(String description, double amount);

    /** Menyimpan pengeluaran baru dan mengembalikannya lengkap dengan ID. */
    Transaction saveExpense(String description, double amount);
}

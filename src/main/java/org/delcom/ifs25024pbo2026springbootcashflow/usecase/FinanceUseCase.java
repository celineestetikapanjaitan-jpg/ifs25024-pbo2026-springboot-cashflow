package org.delcom.ifs25024pbo2026springbootcashflow.usecase;

import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Use case yang menangani logika bisnis catatan keuangan.
 * Tidak melakukan I/O: hanya memproses data dan melempar checked exception
 * domain jika input tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    /** Mengambil semua transaksi. */
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    /** Mencatat pemasukan baru setelah validasi. */
    public Transaction addIncome(String description, double amount) throws ValidationException {
        validate(description, amount);
        return repository.saveIncome(description.trim(), amount);
    }

    /** Mencatat pengeluaran baru setelah validasi. */
    public Transaction addExpense(String description, double amount) throws ValidationException {
        validate(description, amount);
        return repository.saveExpense(description.trim(), amount);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return repository.findBy(t -> t.getDescription().toLowerCase().contains(lowerKeyword));
    }

    /** Mengurutkan transaksi sesuai {@link SortOption}; data asli tidak berubah. */
    public List<Transaction> sortTransactions(SortOption option) {
        return repository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public double getTotalIncome() {
        return totalOf(TransactionType.INCOME);
    }

    /** Total seluruh pengeluaran. */
    public double getTotalExpense() {
        return totalOf(TransactionType.EXPENSE);
    }

    /** Saldo saat ini: total pemasukan dikurangi total pengeluaran. */
    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    private double totalOf(TransactionType type) {
        return repository.findBy(t -> t.getType() == type).stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private void validate(String description, double amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new ValidationException("Jumlah harus berupa angka lebih dari 0");
        }
    }
}

package org.delcom.ifs25024pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.TransactionType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan urusan tampilan dari entity, use case, dan view.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<SortOption, String> SORT_LABELS = Map.of(
            SortOption.AMOUNT_ASC, "Jumlah terkecil",
            SortOption.AMOUNT_DESC, "Jumlah terbesar",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu"
    );

    /** Memformat uang dengan pemisah ribuan koma dan dua desimal, mis. Rp 1,000,000.00. */
    public String formatMoney(double amount) {
        return String.format(Locale.US, "Rp %,.2f", amount);
    }

    /** Nama jenis transaksi dalam bahasa Indonesia. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Memformat waktu pencatatan. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Label opsi pengurutan untuk ditampilkan di UI. */
    public String formatSortOption(SortOption option) {
        return SORT_LABELS.get(option);
    }

    /** Memformat satu transaksi menjadi baris teks: [id] keterangan | Rp ... | [tipe]. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Memformat saldo: Saldo: Rp ... */
    public String showBalance(double balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Memformat daftar transaksi menjadi teks multi-baris; pesan kosong jika tidak ada data. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        if (transactions.isEmpty()) {
            return header + "\n" + emptyMessage;
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        return header + "\n" + body;
    }

    /** Memformat daftar semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Belum ada transaksi!");
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    /** Pesan sukses setelah mencatat transaksi. */
    public String showAddSuccess(Transaction transaction) {
        return "Berhasil mencatat transaksi: " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi yang tidak ada. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan validasi dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}

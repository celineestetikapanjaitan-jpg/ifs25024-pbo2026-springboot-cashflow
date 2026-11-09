package org.delcom.ifs25024pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income = new Transaction(1, "Gaji", 5000000, TransactionType.INCOME,
            LocalDateTime.of(2026, 11, 7, 9, 5));
    private final Transaction expense = new Transaction(2, "Makan", 25000, TransactionType.EXPENSE);

    @Test
    void formatMoneyUsesThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1000000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("Rp 4,975,000.00", presenter.formatMoney(4975000));
        assertEquals("Rp -25,000.50", presenter.formatMoney(-25000.5));
    }

    @Test
    void formatTypeUsesIndonesianNames() {
        assertEquals("Pemasukan", presenter.formatType(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.formatType(TransactionType.EXPENSE));
    }

    @Test
    void formatLineFollowsReferenceFormat() {
        assertEquals("[1] Gaji | Rp 5,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense));
    }

    @Test
    void formatCreatedAtUsesDayMonthYear() {
        assertEquals("07/11/2026 09:05", presenter.formatCreatedAt(income));
    }

    @Test
    void showBalanceFormatsMoney() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.showBalance(4975000));
    }

    @Test
    void formatSortOptionCoversAllOptions() {
        assertEquals("Jumlah terkecil", presenter.formatSortOption(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.formatSortOption(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.formatSortOption(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.formatSortOption(SortOption.EXPENSE_FIRST));
    }

    @Test
    void listsShowRowsOrEmptyMessage() {
        assertEquals("Daftar Transaksi:\n[1] Gaji | Rp 5,000,000.00 | [Pemasukan]",
                presenter.showTransactions(List.of(income)));
        assertEquals("Daftar Transaksi:\n- Belum ada transaksi!", presenter.showTransactions(List.of()));
        assertEquals("Hasil Pencarian: \"x\"\n- Transaksi tidak ditemukan!",
                presenter.showSearchResults(List.of(), "x"));
        assertEquals("Hasil Pencarian: \"Gaji\"\n[1] Gaji | Rp 5,000,000.00 | [Pemasukan]",
                presenter.showSearchResults(List.of(income), "Gaji"));
    }

    @Test
    void messagesAreFormatted() {
        assertEquals("Berhasil mencatat transaksi: [1] Gaji | Rp 5,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 7.", presenter.showRemoveFailed(7));
        assertEquals("[!] Keterangan tidak boleh kosong", presenter.showValidationError("Keterangan tidak boleh kosong"));
    }
}

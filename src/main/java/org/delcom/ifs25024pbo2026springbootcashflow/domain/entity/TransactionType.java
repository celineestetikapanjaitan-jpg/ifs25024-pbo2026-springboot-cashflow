package org.delcom.ifs25024pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan. Urutan deklarasi dipakai oleh {@link SortOption}
 * (INCOME harus tetap lebih dulu daripada EXPENSE).
 */
public enum TransactionType {
    /** Uang masuk (menambah saldo). */
    INCOME,

    /** Uang keluar (mengurangi saldo). */
    EXPENSE
}

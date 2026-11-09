package org.delcom.ifs25024pbo2026springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.delcom.ifs25024pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class FinanceViewTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = spy(new FinanceUseCase(new TransactionRepository()));
        MockVaadin.setup();
        UI.getCurrent().add(new FinanceView(useCase, new FinancePresenter()));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void addSearchSortRefreshAndDeleteFlow() throws Exception {
        assertTrue(_get(Paragraph.class).getText().contains("Belum ada transaksi"));
        assertEquals("Rp 0.00", text("balance"));

        addTransaction("Tambah Pemasukan", "Gaji", 5000000.0);
        assertEquals("Daftar Transaksi", _get(Paragraph.class).getText());
        addTransaction("Tambah Pengeluaran", "Makan", 25000.0);
        addTransaction("Tambah Pengeluaran", "Buku kuliah", 100000.0);

        assertEquals("Rp 5,000,000.00", text("income-total"));
        assertEquals("Rp 125,000.00", text("expense-total"));
        assertEquals("Rp 4,875,000.00", text("balance"));

        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(searchField, "tidakada");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Belum ada transaksi"));

        _setValue(searchField, "GAJI");
        clickButton("Cari");
        assertEquals("Hasil Pencarian: \"GAJI\"", _get(Paragraph.class).getText());
        assertEquals(1, grid().getListDataView().getItemCount());

        _setValue(searchField, "   ");
        clickButton("Cari");
        assertEquals(3, grid().getListDataView().getItemCount());

        @SuppressWarnings("unchecked")
        ComboBox<SortOption> sortBox = _get(ComboBox.class);
        _setValue(sortBox, SortOption.AMOUNT_ASC);
        assertEquals("Makan", firstDescription());
        _setValue(sortBox, SortOption.AMOUNT_DESC);
        assertEquals("Gaji", firstDescription());
        _setValue(sortBox, SortOption.INCOME_FIRST);
        assertEquals("Gaji", firstDescription());
        _setValue(sortBox, SortOption.EXPENSE_FIRST);
        assertTrue(_get(Paragraph.class).getText().contains("Pengeluaran dulu"));

        clickButton("Muat Ulang");
        assertEquals("Daftar Transaksi", _get(Paragraph.class).getText());
        assertEquals(3, grid().getListDataView().getItemCount());

        for (String key : List.of("id", "description", "amount", "type", "createdAt")) {
            grid().sort(List.of(new GridSortOrder<>(grid().getColumnByKey(key), SortDirection.ASCENDING)));
            grid().getListDataView().getItems().toList();
            grid().sort(List.of(new GridSortOrder<>(grid().getColumnByKey(key), SortDirection.DESCENDING)));
            grid().getListDataView().getItems().toList();
        }

        int makanRow = indexOf("Makan");
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid(), makanRow, "actions");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        clickDialogButton(_get(Dialog.class), "Hapus");

        assertEquals(2, useCase.getAllTransactions().size());
        assertEquals("Rp 4,900,000.00", text("balance"));
    }

    @Test
    void rejectsInvalidInputAndKeepsDialogOpenUntilCancelled() {
        clickButton("Tambah Pemasukan");
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class), " ");
        _setValue(_get(dialog, NumberField.class), 100.0);
        clickDialogButton(dialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());

        _setValue(_get(dialog, TextField.class), "Gaji");
        _setValue(_get(dialog, NumberField.class), -5.0);
        clickDialogButton(dialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());
        clickDialogButton(dialog, "Batal");

        clickButton("Tambah Pengeluaran");
        Dialog expenseDialog = _get(Dialog.class);
        _setValue(_get(expenseDialog, TextField.class), "Makan");
        clickDialogButton(expenseDialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());
        clickDialogButton(expenseDialog, "Batal");
    }

    @Test
    void deleteCanBeCancelledAndHandlesMissingTransaction() throws Exception {
        useCase.addIncome("Gaji", 100);
        clickButton("Muat Ulang");

        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid(), 0, "actions");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        clickDialogButton(_get(Dialog.class), "Batal");
        assertEquals(1, useCase.getAllTransactions().size());

        Transaction selected = useCase.getAllTransactions().get(0);
        doThrow(new EntityNotFoundException(selected.getId())).when(useCase).removeTransaction(selected.getId());
        HorizontalLayout actions2 = (HorizontalLayout) _getCellComponent(grid(), 0, "actions");
        _click(_get(actions2, Button.class, spec -> spec.withCaption("Hapus")));
        clickDialogButton(_get(Dialog.class), "Hapus");
        assertEquals(1, useCase.getAllTransactions().size());
    }

    // ------------------------------------------------------------------ helper

    private void addTransaction(String buttonCaption, String description, double amount) {
        clickButton(buttonCaption);
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class), description);
        _setValue(_get(dialog, NumberField.class), amount);
        clickDialogButton(dialog, "Simpan");
    }

    @SuppressWarnings("unchecked")
    private Grid<Transaction> grid() {
        return _get(Grid.class);
    }

    private String firstDescription() {
        return grid().getListDataView().getItems().toList().get(0).getDescription();
    }

    private int indexOf(String description) {
        List<Transaction> items = grid().getListDataView().getItems().toList();
        for (int i = 0; i < items.size(); i++) {
            if (description.equals(items.get(i).getDescription())) {
                return i;
            }
        }
        throw new AssertionError("Transaksi tidak ditemukan: " + description);
    }

    private String text(String id) {
        return _get(Span.class, spec -> spec.withId(id)).getText();
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }
}

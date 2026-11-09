package org.delcom.ifs25024pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25024pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi catatan keuangan.
 * View hanya memanggil use case dan presenter; tidak ada aturan bisnis di sini.
 */
@Route("")
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private static final String EMPTY_MESSAGE = "Belum ada transaksi!";

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_MESSAGE);
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span();
    private final Span expenseTotal = new Span();
    private final Span balanceLabel = new Span();

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter) {
        this.useCase = useCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        incomeTotal.setId("income-total");
        expenseTotal.setId("expense-total");
        balanceLabel.setId("balance");

        configureGrid();
        configureToolbar();

        add(new H1("Cashflow"), buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    // ---------------------------------------------------------------- ringkasan

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCount, "#E0E7FF", "#312E81"),
                createSummaryCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#14532D"),
                createSummaryCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#7F1D1D"),
                createSummaryCard("Saldo", balanceLabel, "#DBEAFE", "#1E3A8A")
        );
        summary.setWidthFull();
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div createSummaryCard(String label, Span value, String background, String accent) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", accent);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("background", background)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem");
        return card;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(useCase.getAllTransactions().size()));
        incomeTotal.setText(presenter.formatMoney(useCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatMoney(useCase.getTotalExpense()));
        balanceLabel.setText(presenter.formatMoney(useCase.getBalance()));
    }

    // --------------------------------------------------------------------- grid

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparingInt(Transaction::getId));

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(Transaction::getAmount));

        grid.addColumn(transaction -> presenter.formatType(transaction.getType()))
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        applyColors(deleteButton, "transparent", "#991B1B");
        return new HorizontalLayout(deleteButton);
    }

    // ------------------------------------------------------------------ toolbar

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortBox.setPlaceholder("Urutkan...");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::formatSortOption);
        sortBox.addValueChangeListener(event -> applySort(event.getValue()));
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.ARROW_DOWN.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        applyColors(incomeButton, "#166534", "#FFFFFF");

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.ARROW_UP.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        applyColors(expenseButton, "#991B1B", "#FFFFFF");

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortBox, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    /** Warna tombol dengan kontras WCAG AA (rasio >= 4.5:1). */
    private void applyColors(Button button, String background, String foreground) {
        button.getStyle()
                .set("--vaadin-button-background", background)
                .set("--vaadin-button-text-color", foreground)
                .set("--vaadin-button-border-color", "transparent")
                .set("background", background)
                .set("color", foreground);
    }

    // ------------------------------------------------------------------- dialog

    private void openAddDialog(TransactionType type) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();

        NumberField amountField = new NumberField("Jumlah (Rp)");
        amountField.setWidthFull();

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            try {
                double amount = amountField.getValue() == null ? Double.NaN : amountField.getValue();
                Transaction created = type == TransactionType.INCOME
                        ? useCase.addIncome(descriptionField.getValue(), amount)
                        : useCase.addExpense(descriptionField.getValue(), amount);
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyColors(saveButton, "#1E3A8A", "#FFFFFF");

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        String title = type == TransactionType.INCOME ? "Tambah Pemasukan" : "Tambah Pengeluaran";
        buildDialog(dialog, title, new VerticalLayout(descriptionField, amountField), cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph("Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                + "\" (ID " + transaction.getId() + ")?");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        applyColors(confirmButton, "#991B1B", "#FFFFFF");

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", message, cancelButton, confirmButton);
        dialog.open();
    }

    /** Merakit dialog: judul, isi, lalu baris tombol di bawah. */
    private void buildDialog(Dialog dialog, String title, Component body, Button... footerButtons) {
        dialog.setWidth("480px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);

        HorizontalLayout footer = new HorizontalLayout(footerButtons);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(new Div(new H3(title), body, footer));
    }

    // --------------------------------------------------------------------- aksi

    private void deleteTransaction(Transaction transaction) {
        try {
            useCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        refreshGrid(useCase.searchTransactions(keyword), "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void applySort(SortOption option) {
        if (option != null) {
            refreshGrid(useCase.sortTransactions(option), "Daftar Transaksi (" + presenter.formatSortOption(option) + ")");
        }
    }

    private void refreshAll() {
        sortBox.clear();
        searchField.clear();
        refreshGrid(useCase.getAllTransactions(), "Daftar Transaksi");
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        statusLabel.setText(transactions.isEmpty() ? title + " — " + EMPTY_MESSAGE : title);
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 4000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

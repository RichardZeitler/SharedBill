package htw.university.sharedbill.controller;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.Serializable;
import java.util.List;

import htw.university.sharedbill.R;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.Invoice;
import htw.university.sharedbill.model.invoice.InvoiceUtils;

public class InvoiceHistoryActivity extends AppCompatActivity {
    private LinearLayout invoiceContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invoice_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.invoiceHistory), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        invoiceContainer = findViewById(R.id.invoiceContainer);

        if (EateryInvoice.INVOICES == null || EateryInvoice.INVOICES.isEmpty()) {
            Toast.makeText(this, "Keine Rechnungen vorhanden.", Toast.LENGTH_SHORT).show();
        } else {
            loadInvoicesFromList(EateryInvoice.INVOICES);
        }
    }

    private void loadInvoicesFromList(List<Invoice> invoiceList) {
        invoiceContainer.removeAllViews();
        for (Invoice invoice : invoiceList) {
            View invoiceItem = createInvoiceItemView(invoice, invoiceList);
            invoiceContainer.addView(invoiceItem);
            invoiceContainer.addView(createDivider());
        }
    }

    private View createInvoiceItemView(Invoice invoice, List<Invoice> invoiceList) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, dpToPx(10), 0, 0);
        itemLayout.setLayoutParams(layoutParams);

        TextView itemText = new TextView(this);
        itemText.setText(String.format("Rechnung: # %s - %s", invoice.getInvoiceID(), invoice.getDate().toString()));
        itemText.setTextSize(18);
        itemText.setTypeface(null, Typeface.BOLD);
        itemText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        ImageView menuIcon = new ImageView(this);
        menuIcon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(30), dpToPx(30)));
        menuIcon.setImageResource(R.drawable.dots); // 3-Punkte-Menü Icon
        menuIcon.setContentDescription("Menü anzeigen");

        menuIcon.setOnClickListener(view -> showPopupMenu(view, invoice, invoiceList));

        itemLayout.addView(itemText);
        itemLayout.addView(menuIcon);

        return itemLayout;
    }

    private void showPopupMenu(View anchor, Invoice invoice, List<Invoice> invoiceList) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add("Anzeigen");
        popupMenu.getMenu().add("Bearbeiten");
        popupMenu.getMenu().add("Teilen");

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String title = menuItem.getTitle().toString();
            switch (title) {
                case "Anzeigen":
                    Intent intent = new Intent(this, InvoiceShowAcitivity.class);
                    intent.putExtra("invoice", (Serializable) invoice);
                    startActivity(intent);
                    return true;
                case "Teilen":
                    Toast.makeText(this, "In Entwicklung: " + invoice.getInvoiceID(), Toast.LENGTH_SHORT).show();
                    return true;

                case "Löschen":
                    boolean deleted = InvoiceUtils.deleteInvoiceFromStorage(this, invoice);
                    if (deleted) {
                        EateryInvoice.INVOICES.remove(invoice);
                        invoiceList.remove(invoice);
                        loadInvoicesFromList(invoiceList);
                        Toast.makeText(this, "Rechnung gelöscht.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Fehler beim Löschen.", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private View createDivider() {
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
        );
        dividerParams.setMargins(0, dpToPx(5), 0, 0);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.LTGRAY);
        return divider;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

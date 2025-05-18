package htw.university.sharedbill.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import htw.university.sharedbill.R;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.Invoice;
import htw.university.sharedbill.model.invoice.InvoiceUtils;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.startView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        List<Invoice> invoiceList = InvoiceUtils.loadAllInvoices(this);
        invoiceList.forEach(invoice -> EateryInvoice.INVOICES.add(invoice));

        ConstraintLayout createInvoiceButton = findViewById(R.id.addInvoiceButton);
        createInvoiceButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, CreateInvoiceActivity.class);
            startActivity(intent);
        });

        ConstraintLayout invoiceHistory = findViewById(R.id.invoiceHistoryButton);
        invoiceHistory.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, InvoiceHistoryActivity.class);
            startActivity(intent);
        });
    }
}

package htw.university.sharedbill.controller.invoce;

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
import htw.university.sharedbill.controller.bluetooth.SelectDeviceActivity;
import htw.university.sharedbill.model.invoice.InvoiceUtils;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;

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

        List<InvoiceWrapper> invoiceList = InvoiceUtils.loadAllInvoices(this);
        invoiceList.forEach(invoice -> InvoiceWrapper.INVOICES.add(invoice));

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

        ConstraintLayout searchDevices = findViewById(R.id.searchDevicesButton);
        searchDevices.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, SelectDeviceActivity.class);
            intent.putExtra("title", "Bluetooth-Geräte suchen");
            intent.putExtra("disableShareInvoice", true);
            startActivity(intent);
        });
    }
}

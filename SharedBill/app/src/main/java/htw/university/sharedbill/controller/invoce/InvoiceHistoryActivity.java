package htw.university.sharedbill.controller.invoce;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import htw.university.sharedbill.R;
import htw.university.sharedbill.adapter.InvoiceAdapter;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;

public class InvoiceHistoryActivity extends AppCompatActivity {

    private RecyclerView invoiceRecyclerView;

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


        invoiceRecyclerView = findViewById(R.id.invoiceRecyclerView);
        invoiceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (InvoiceWrapper.INVOICES == null || InvoiceWrapper.INVOICES.isEmpty()) {
            Toast.makeText(this, "Keine Rechnungen vorhanden.", Toast.LENGTH_SHORT).show();
        } else {
            InvoiceAdapter adapter = new InvoiceAdapter(this);
            invoiceRecyclerView.setAdapter(adapter);
        }
    }
}

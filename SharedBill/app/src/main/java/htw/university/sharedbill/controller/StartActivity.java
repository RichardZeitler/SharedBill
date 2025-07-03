package htw.university.sharedbill.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import htw.university.sharedbill.R;
import htw.university.sharedbill.controller.bluetooth.SelectDeviceActivity;
import htw.university.sharedbill.controller.invoce.CreateInvoiceActivity;
import htw.university.sharedbill.controller.invoce.InvoiceHistoryActivity;
import htw.university.sharedbill.model.invoice.StorageUtils;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;

/**
 * StartActivity ist die Einstiegs-Activity der App.
 * Sie zeigt die Startseite mit Buttons zum Erstellen einer neuen Rechnung,
 * zum Anzeigen der Rechnungshistorie sowie zum Suchen von Bluetooth-Geräten.
 *
 * Beim Start werden bereits gespeicherte Rechnungen geladen und in eine zentrale Liste eingefügt.
 */
public class StartActivity extends AppCompatActivity {

    /**
     * Initialisiert die Activity, richtet die UI ein und lädt gespeicherte Rechnungen.
     *
     * @param savedInstanceState Wenn die Activity neu erstellt wird, enthält dieses Bundle
     *                           die zuletzt gespeicherten Zustandsdaten.
     */
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

        // Lädt alle gespeicherten Rechnungen und fügt sie der zentralen Liste hinzu
        try {
            List<InvoiceWrapper> wrapperList = StorageUtils.loadAllInvoices(this);
            wrapperList.forEach(invoiceWrapper -> InvoiceWrapper.INVOICES.add(invoiceWrapper));
            Log.i("StorageUtils", "Rechnungen wurden geladen");
        } catch (IOException | JSONException e) {
            Log.e("StorageUtilsException", e.getMessage());
        }

        // Button zum Erstellen einer neuen Rechnung
        ConstraintLayout createInvoiceButton = findViewById(R.id.addInvoiceButton);
        createInvoiceButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, CreateInvoiceActivity.class);
            startActivity(intent);
            Log.i("StartController", "Wechsel zu CreateInvoiceActivity");
        });

        // Button zum Öffnen der Rechnungshistorie
        ConstraintLayout invoiceHistory = findViewById(R.id.invoiceHistoryButton);
        invoiceHistory.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, InvoiceHistoryActivity.class);
            startActivity(intent);
            Log.i("StartController", "Wechsel zu InvoiceHistoryActivity");
        });

        // Button zum Starten der Bluetooth-Gerätesuche
        ConstraintLayout searchDevices = findViewById(R.id.searchDevicesButton);
        searchDevices.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, SelectDeviceActivity.class);
            intent.putExtra("title", "Bluetooth-Geräte suchen");
            intent.putExtra("disableShareInvoice", true);
            startActivity(intent);
            Log.i("StartController", "Wechsel zu SelectDeviceActivity");
        });
    }
}

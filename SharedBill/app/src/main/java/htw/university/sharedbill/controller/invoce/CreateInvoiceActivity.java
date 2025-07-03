package htw.university.sharedbill.controller.invoce;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import htw.university.sharedbill.R;
import htw.university.sharedbill.model.invoice.Address;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.Invoice;
import htw.university.sharedbill.model.invoice.InvoiceObserver;
import htw.university.sharedbill.model.invoice.StorageUtils;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;
import htw.university.sharedbill.model.invoice.Item;

/**
 * Activity zum Erstellen einer neuen Rechnung.
 * Diese Klasse ermöglicht das Erfassen von Rechnungsdaten,
 * das Hinzufügen und Entfernen von Positionen sowie das Speichern der Rechnung.
 */
public class CreateInvoiceActivity extends AppCompatActivity implements InvoiceObserver {
    private EateryInvoice eateryInvoice;
    private LinearLayout itemContainer;
    private EditText editItemName;
    private EditText editItemDescription;
    private EditText editItemGrossPrice;
    private EditText editItemTaxRate;
    private TextView showTotalPrice;
    private EditText editTotalGrossPrice;
    private EditText editTotalNetPrice;
    private EditText editTotalTaxPrice;
    private EditText editIssuerName;
    private EditText editIssuerStreet;
    private EditText editIssuerZip;
    private EditText editIssuerCity;
    private EditText editIssuerCountry;
    private EditText editInvoiceId;
    private EditText editDate;
    private EditText editVatId;
    private EditText editTransactionId;
    private EditText editCheckSum;
    private EditText editDeviceId;
    private EditText editPaymentMethod;
    private ViewGroup root;
    private Button createInvoiceButton, addItemButton;

    /**
     * Methode, die beim Erstellen der Activity aufgerufen wird.
     * Initialisiert die Benutzeroberfläche und Listener.
     * @param savedInstanceState vorheriger Zustand der Activity (kann null sein)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invoice_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createInvoice), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eateryInvoice = new EateryInvoice();

        initViews();
        initListeners();
    }

    /**
     * Initialisiert alle View-Elemente der Activity.
     */
    private void initViews() {
        createInvoiceButton = findViewById(R.id.addInvoice);
        root = findViewById(R.id.createInvoice);
        itemContainer = findViewById(R.id.itemContainer);
        editItemName = findViewById(R.id.editItemName);
        editItemDescription = findViewById(R.id.editItemDescription);
        editItemGrossPrice = findViewById(R.id.editItemGrossPrice);
        editItemTaxRate = findViewById(R.id.editItemTaxRate);
        showTotalPrice = findViewById(R.id.showTotalPrice);
        editTotalGrossPrice = findViewById(R.id.editTotalGrossPrice);
        editTotalNetPrice = findViewById(R.id.editTotalNetPrice);
        editTotalTaxPrice = findViewById(R.id.editTotalTaxPrice);
        editIssuerName = findViewById(R.id.editIssuerName);
        editIssuerStreet = findViewById(R.id.editIssuerStreet);
        editIssuerZip = findViewById(R.id.editIssuerZip);
        editIssuerCity = findViewById(R.id.editIssuerCity);
        editIssuerCountry = findViewById(R.id.editIssuerCountry);
        editInvoiceId = findViewById(R.id.editInvoiceId);
        editDate = findViewById(R.id.editDate);
        editVatId = findViewById(R.id.editVatId);
        editTransactionId = findViewById(R.id.editTransactionId);
        editCheckSum = findViewById(R.id.editCheckSum);
        editDeviceId = findViewById(R.id.editDeviceId);
        editPaymentMethod = findViewById(R.id.editPaymentMethod);
        addItemButton = findViewById(R.id.addItem);
    }

    /**
     * Initialisiert alle Listener für Buttons und beobachtet Änderungen an der Rechnung.
     */
    private void initListeners() {
        eateryInvoice.addObserver(this);
        addItemButton.setOnClickListener(v -> addItem());

        createInvoiceButton.setOnClickListener(v -> {
            createInvoice();

            if (eateryInvoice.isValid()) {
                InvoiceWrapper invoiceWrapper = new InvoiceWrapper(eateryInvoice, "Unbezahlt");
                if (!InvoiceWrapper.INVOICES.contains(invoiceWrapper)) {
                    try {
                        StorageUtils.saveInvoiceToAppStorage(this, invoiceWrapper);
                        InvoiceWrapper.INVOICES.add(invoiceWrapper);

                        Intent intent = new Intent(CreateInvoiceActivity.this, InvoiceHistoryActivity.class);
                        startActivity(intent);

                        Log.i("CreateInvoiceController", "Rechnung wurde erstellt");
                        Log.i("StorageUtils", "Rechnung wurde gespeichert.");
                    } catch (IOException | JSONException e) {
                        Log.e("CreateInvoiceController", "Das Speichern der Rechnung ist fehlgeschlagen");
                    } finally {
                        clearAllEditTexts(root);
                        showTotalPrice.setText("0€");
                    }
                }
            } else {
                Toast.makeText(this, "Rechnung existiert bereits.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fügt ein neues Item zur Rechnung hinzu und zeigt es in der UI an.
     * Bei fehlerhaften Eingaben wird eine Fehlermeldung angezeigt.
     */
    public void addItem() {
        try {
            String itemName = editItemName.getText().toString().trim();
            String itemDescription = editItemDescription.getText().toString().trim();
            double itemGrossPrice = Double.parseDouble(editItemGrossPrice.getText().toString().trim());
            double itemTaxRate = Double.parseDouble(editItemTaxRate.getText().toString().trim());

            Item item = (itemDescription != null && !itemDescription.isEmpty())
                    ? new Item(itemName, itemDescription, itemGrossPrice, itemTaxRate)
                    : new Item(itemName, itemGrossPrice, itemTaxRate);

            eateryInvoice.addItem(item);

            View itemView = getLayoutInflater().inflate(R.layout.item_layout_delete, itemContainer, false);

            TextView itemText = itemView.findViewById(R.id.itemText);
            itemText.setText(item.toString());

            ImageView delteButton = itemView.findViewById(R.id.itemDelete);
            delteButton.setOnClickListener(v -> removeItem(itemView, item));

            itemContainer.addView(itemView);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "[Item] Bruttobetrag oder Mehrwertsteuer des Items ist ungültig.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        } finally {
            editItemTaxRate.setText("");
            editItemDescription.setText("");
            editItemGrossPrice.setText("");
            editItemName.setText("");

            Toast.makeText(this, "Item wurde hinzugefügt.", Toast.LENGTH_SHORT).show();
            Log.i("CreateInvoiceController", "Item wurde hinzugefügt");
        }
    }

    /**
     * Entfernt ein Item aus der Rechnung und aus der UI.
     *
     * @param itemView die View des zu entfernenden Items
     * @param item     das zu entfernende Item
     */
    private void removeItem(View itemView, Item item) {
        try {
            itemContainer.removeView(itemView);
            eateryInvoice.removeItem(item);
        } catch (Exception e) {
            Log.e("CreateInvoiceController", e.getMessage());
        }
    }

    /**
     * Liest die Eingabefelder aus und befüllt die EateryInvoice mit den Daten.
     * Zeigt Fehlermeldungen bei ungültigen Eingaben an.
     */
    private void createInvoice() {
        try {
            eateryInvoice.setInvoiceID(editInvoiceId.getText().toString().trim());
            eateryInvoice.setPaymentMethod(editPaymentMethod.getText().toString().trim());
            eateryInvoice.setVatID(editVatId.getText().toString().trim());
            eateryInvoice.setTrnsactionID(editTransactionId.getText().toString().trim());
            eateryInvoice.setCheckSum(editCheckSum.getText().toString().trim());
            eateryInvoice.setDeviceID(editDeviceId.getText().toString().trim());

            eateryInvoice.setDate(parseInvoiceDate(editDate.getText().toString().trim()));
            eateryInvoice.setIssuer(createIssuerAddress());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "[Adresse] Postleitzahl ist ungültig.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Wandelt einen Datums-String im Format "dd.MM.yyyy" in ein LocalDateTime-Objekt um.
     * Die Uhrzeit wird auf die aktuelle Uhrzeit gesetzt.
     *
     * @param dateString das Datum als String
     * @return das LocalDateTime-Objekt mit Datum und aktueller Uhrzeit
     */
    private LocalDateTime parseInvoiceDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate date = LocalDate.parse(dateString, formatter);
        LocalTime time = LocalTime.now().withNano(0);
        return LocalDateTime.of(date, time);
    }

    /**
     * Erstellt aus den Eingabefeldern eine Adresse für den Rechnungsaussteller.
     *
     * @return die Adresse des Ausstellers
     * @throws NumberFormatException wenn die Postleitzahl ungültig ist
     */
    private Address createIssuerAddress() {
        String issuerName = editIssuerName.getText().toString().trim();
        String issuerStreet = editIssuerStreet.getText().toString().trim();
        int issuerZip = Integer.parseInt(editIssuerZip.getText().toString().trim());
        String issuerCity = editIssuerCity.getText().toString().trim();
        String issuerCountry = editIssuerCountry.getText().toString().trim();
        return new Address(issuerName, issuerStreet, issuerZip, issuerCity, issuerCountry);
    }

    /**
     * Löscht alle EditText-Felder rekursiv aus einem ViewGroup-Container.
     *
     * @param root das Wurzel-ViewGroup-Element
     */
    private void clearAllEditTexts(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof EditText) {
                ((EditText) child).setText("");
            } else if (child instanceof ViewGroup) {
                clearAllEditTexts((ViewGroup) child);
            }
        }
    }

    /**
     * Wird aufgerufen, wenn sich die Rechnung ändert.
     * Aktualisiert die Anzeige der Gesamtsummen.
     *
     * @param invoice die aktuelle Rechnung
     */
    @Override
    public void updateInvoice(Invoice invoice) {
        showTotalPrice.setText(invoice.getGrossPrice() + "€");
        editTotalGrossPrice.setText(invoice.getGrossPrice() + "€");
        editTotalNetPrice.setText(invoice.getNetPrice() + "€");
        editTotalTaxPrice.setText(invoice.getTaxPrice() +"€");
    }
}

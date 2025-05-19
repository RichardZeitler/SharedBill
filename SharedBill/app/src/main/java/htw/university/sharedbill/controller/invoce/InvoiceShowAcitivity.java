package htw.university.sharedbill.controller.invoce;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Map;

import htw.university.sharedbill.R;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;
import htw.university.sharedbill.model.invoice.Item;

public class InvoiceShowAcitivity extends AppCompatActivity {
    private TextView showTotalPrice;
    private EditText showTotalGrossPrice;
    private EditText showTotalNetPrice;
    private EditText showTotalTaxPrice;
    private EditText showIssuerName;
    private EditText showIssuerStreet;
    private EditText showIssuerZip;
    private EditText showIssuerCity;
    private EditText showIssuerCountry;
    private EditText showInvoiceId;
    private EditText showDate;
    private EditText showVatId;
    private EditText showTransactionId;
    private EditText showCheckSum;
    private EditText showDeviceId;
    private EditText showPaymentMethod;
    private TextView changePaymentStatus;

    private LinearLayout showItemContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_invoice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.showInvoice), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        showItemContainer = findViewById(R.id.showItemContainer);

        showTotalPrice = findViewById(R.id.textView100);
        showTotalGrossPrice = findViewById(R.id.showTotalGrossPrice);
        showTotalNetPrice = findViewById(R.id.showTotalNetPrice);
        showTotalTaxPrice = findViewById(R.id.showTotalTaxPrice);
        showIssuerName = findViewById(R.id.showIssuerName);
        showIssuerStreet = findViewById(R.id.showIssuerStreet);
        showIssuerZip = findViewById(R.id.showIssuerZip);
        showIssuerCity = findViewById(R.id.showIssuerCity);
        showIssuerCountry = findViewById(R.id.showIssuerCountry);
        showInvoiceId = findViewById(R.id.showInvoiceId);
        showDate = findViewById(R.id.showDate);
        showVatId = findViewById(R.id.showVatId);
        showTransactionId = findViewById(R.id.showTransactionId);
        showCheckSum = findViewById(R.id.showCheckSum);
        showDeviceId = findViewById(R.id.showDeviceId);
        showPaymentMethod = findViewById(R.id.showPaymentMethod);
        changePaymentStatus = findViewById(R.id.changePaymentStatus);

        InvoiceWrapper invoiceWrapper = (InvoiceWrapper) getIntent().getSerializableExtra("invoice");
        loadInvoice(invoiceWrapper);
    }

    private void loadInvoice(InvoiceWrapper invoiceWrapper) {
        try {
            EateryInvoice eateryInvoice = (EateryInvoice) invoiceWrapper.getInvoice();

            showDeviceId.setText(eateryInvoice.getDeviceID());
            showTotalPrice.setText(String.format("%.2f", eateryInvoice.getGrossPrice()) + "€");
            showTotalGrossPrice.setText(String.format("%.2f", eateryInvoice.getGrossPrice()) + "€");
            showTotalNetPrice.setText(String.format("%.2f", eateryInvoice.getNetPrice()) + "€");
            showTotalTaxPrice.setText(String.format("%.2f", eateryInvoice.getTaxPrice()) + "€");
            showIssuerName.setText(eateryInvoice.getIssuer().getName());
            showIssuerStreet.setText(eateryInvoice.getIssuer().getStreet());
            showIssuerZip.setText(String.valueOf(eateryInvoice.getIssuer().getZip()));
            showIssuerCity.setText(eateryInvoice.getIssuer().getCity());
            showIssuerCountry.setText(eateryInvoice.getIssuer().getCountry());
            showInvoiceId.setText(eateryInvoice.getInvoiceID());
            showDate.setText(eateryInvoice.getDate().toString());
            showVatId.setText(eateryInvoice.getVatID());
            showTransactionId.setText(eateryInvoice.getTransactionID());
            showCheckSum.setText(eateryInvoice.getCheckSum());
            showPaymentMethod.setText(eateryInvoice.getPaymentMethod());
            changePaymentStatus.setText(invoiceWrapper.getPaymentStatus());

            for (Map.Entry<Item, Integer> entry: eateryInvoice.getItems().entrySet()) {
                Item item = entry.getKey();
                int amount = entry.getValue();

                for (int i = 0; i < amount; i++) {
                    addItem(item);
                }
            }
        } catch (NullPointerException e) {
            Toast.makeText(this, "Fehler beim Laden der Rechnung.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addItem(Item item) {
        try {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            layoutParams.setMargins(0, dpToPx(10), 0 ,0);

            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setLayoutParams(layoutParams);

            TextView itemText = new TextView(this);
            itemText.setText(item.toString());
            itemText.setTextSize(18);
            itemText.setTypeface(null, Typeface.BOLD);
            itemText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            itemLayout.addView(itemText);

            showItemContainer.addView(itemLayout);
            Toast.makeText(this, "Rechung wurde erfolgreich geladen.", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "[Item] Bruttobetrag oder Mehrwertsteuer des Items ist ungültig.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
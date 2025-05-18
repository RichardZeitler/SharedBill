package htw.university.sharedbill.controller;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
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

import java.util.Map;

import htw.university.sharedbill.R;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.Invoice;
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

        Invoice invoice = (Invoice) getIntent().getSerializableExtra("invoice");
        loadInvoice(invoice);
    }

    private void loadInvoice(Invoice invoice) {
        try {
            EateryInvoice eateryInvoice = (EateryInvoice) invoice;

            showDeviceId.setText(eateryInvoice.getDeviceID());
            showTotalPrice.setText(String.format("%.2f", invoice.getGrossPrice()) + "€");
            showTotalGrossPrice.setText(String.format("%.2f", invoice.getGrossPrice()) + "€");
            showTotalNetPrice.setText(String.format("%.2f", invoice.getNetPrice()) + "€");
            showTotalTaxPrice.setText(String.format("%.2f", invoice.getTaxPrice()) + "€");
            showIssuerName.setText(invoice.getIssuer().getName());
            showIssuerStreet.setText(invoice.getIssuer().getStreet());
            showIssuerZip.setText(String.valueOf(invoice.getIssuer().getZip()));
            showIssuerCity.setText(invoice.getIssuer().getCity());
            showIssuerCountry.setText(invoice.getIssuer().getCountry());
            showInvoiceId.setText(invoice.getInvoiceID());
            showDate.setText(invoice.getDate().toString());
            showVatId.setText(invoice.getVatID());
            showTransactionId.setText(invoice.getTransactionID());
            showCheckSum.setText(invoice.getCheckSum());
            showPaymentMethod.setText(invoice.getPaymentMethod());

            for (Map.Entry<Item, Integer> entry: invoice.getItems().entrySet()) {
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

            ImageView deleteButton = new ImageView(this);
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(30), dpToPx(30)));

            deleteButton.setImageResource(R.drawable.delete);
            deleteButton.setContentDescription("Delete");

            itemLayout.addView(itemText);
            itemLayout.addView(deleteButton);

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
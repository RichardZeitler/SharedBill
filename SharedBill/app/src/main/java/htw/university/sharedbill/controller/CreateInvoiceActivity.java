package htw.university.sharedbill.controller;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import htw.university.sharedbill.R;
import htw.university.sharedbill.model.invoice.Address;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.Invoice;
import htw.university.sharedbill.model.invoice.InvoiceObserver;
import htw.university.sharedbill.model.invoice.InvoiceUtils;
import htw.university.sharedbill.model.invoice.Item;

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

        Button createInvoiceButton = findViewById(R.id.addInvoice);
        createInvoiceButton.setOnClickListener(v -> {
            createInvoice();
            if (eateryInvoice.isValid()) {
                EateryInvoice.INVOICES.add(eateryInvoice);
                InvoiceUtils.saveInvoiceToAppStorage(this, eateryInvoice);
                clearAllEditTexts(root);
                showTotalPrice.setText("0€");
                Intent intent = new Intent(CreateInvoiceActivity.this, InvoiceHistoryActivity.class);
                startActivity(intent);            }
        });

        Button addItemButton = findViewById(R.id.addItem);
        addItemButton.setOnClickListener(v -> addItem());

        eateryInvoice = new EateryInvoice();
        eateryInvoice.addObserver(this);
    }

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

            deleteButton.setOnClickListener(v -> removeItem(itemLayout, item));

            itemLayout.addView(itemText);
            itemLayout.addView(deleteButton);

            itemContainer.addView(itemLayout);

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
        }
    }

    public void removeItem(LinearLayout itemLayout, Item item) {
        try {
            itemContainer.removeView(itemLayout);
            eateryInvoice.removeItem(item);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void createInvoice() {
        try {
            eateryInvoice.setInvoiceID(editInvoiceId.getText().toString().trim());
            eateryInvoice.setPaymentMethod(editPaymentMethod.getText().toString().trim());
            eateryInvoice.setVatID(editVatId.getText().toString().trim());
            eateryInvoice.setTrnsactionID(editTransactionId.getText().toString().trim());
            eateryInvoice.setCheckSum(editCheckSum.getText().toString().trim());
            eateryInvoice.setDeviceID(editDeviceId.getText().toString().trim());

            String dateString = editDate.getText().toString().trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate date = LocalDate.parse(dateString, formatter);
            LocalTime time = LocalTime.now().withNano(0);

            eateryInvoice.setDate(LocalDateTime.of(date, time));

            String issuerName = editIssuerName.getText().toString().trim();
            String issuerStreet = editIssuerStreet.getText().toString().trim();
            int issuerZip = Integer.parseInt(editIssuerZip.getText().toString().trim());
            String issuerCity = editIssuerCity.getText().toString().trim();
            String issuerCountry = editIssuerCountry.getText().toString().trim();

            Address issuer = new Address(issuerName, issuerStreet, issuerZip, issuerCity, issuerCountry);

            eateryInvoice.setIssuer(issuer);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "[Address] Postleitzahl ist ungültig.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

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

    @Override
    public void updateInvoice(Invoice invoice) {
        showTotalPrice.setText(String.format("%.2f", invoice.getGrossPrice()) + "€");
        editTotalGrossPrice.setText(String.format("%.2f", invoice.getGrossPrice()) + "€");
        editTotalNetPrice.setText(String.format("%.2f", invoice.getNetPrice()) + "€");
        editTotalTaxPrice.setText(String.format("%.2f", invoice.getTaxPrice()) +"€");
    }
}
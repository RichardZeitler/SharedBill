package htw.university.sharedbill.controller.invoce;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import htw.university.sharedbill.R;
import htw.university.sharedbill.controller.bluetooth.SelectDeviceActivity;
import htw.university.sharedbill.model.bluetooth.BluetoothLeManager;
import htw.university.sharedbill.model.bluetooth.MessageListener;
import htw.university.sharedbill.model.bluetooth.SimpleGattClient;
import htw.university.sharedbill.model.bluetooth.SimpleGattServer;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;
import htw.university.sharedbill.model.invoice.Item;

public class InvoiceShowAcitivity extends AppCompatActivity implements MessageListener {
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

    private SimpleGattServer simpleGattServer;
    private SimpleGattClient simpleGattClient;
    private Map<Integer, Integer> availableItems = new HashMap<>();
    private EateryInvoice partialInvoice;

    private Queue<Pair<BluetoothDevice, JSONObject>> queue = new ConcurrentLinkedQueue<>();
    private boolean isProcessing = false;

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

        initViews();

        boolean sharedInvoice = getIntent().getBooleanExtra("sharedInvoice", false);
        InvoiceWrapper invoiceWrapper = (InvoiceWrapper) getIntent().getSerializableExtra("invoice");

        if (sharedInvoice) {
            simpleGattServer = BluetoothLeManager.getInstance(this,getSystemService(BluetoothManager.class)).getServer();
            simpleGattClient = BluetoothLeManager.getInstance(this, getSystemService(BluetoothManager.class)).getClient();

            simpleGattServer.registerDataListener(this);
            simpleGattClient.registerMessageListener(this);

        }

        loadInvoiceData(invoiceWrapper, sharedInvoice);

        partialInvoice = new EateryInvoice();
    }

    private void initViews(){
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
    }

    private void loadInvoiceData(InvoiceWrapper invoiceWrapper, boolean sharedInvoice) {
        try {
            EateryInvoice eateryInvoice = (EateryInvoice) invoiceWrapper.getInvoice();

            if (!sharedInvoice) {
                showTotalPrice.setText(String.format("%.2f", eateryInvoice.getGrossPrice()) + "€");
                showTotalGrossPrice.setText(String.format("%.2f", eateryInvoice.getGrossPrice()) + "€");
                showTotalNetPrice.setText(String.format("%.2f", eateryInvoice.getNetPrice()) + "€");
                showTotalTaxPrice.setText(String.format("%.2f", eateryInvoice.getTaxPrice()) + "€");
            }

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
                    addItem(item, sharedInvoice);
                    availableItems.put(item.hashCode(), availableItems.getOrDefault(item.hashCode(), 0));
                }
            }

            Log.d("InvoiceJSON", invoiceWrapper.getJSONObject().toString());
        } catch (NullPointerException e) {
            Toast.makeText(this, "Fehler beim Laden der Rechnung.", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void addItem(Item item, boolean sharedInvoice) {
        View itemView = sharedInvoice ? getLayoutInflater().inflate(R.layout.item_layout_select, showItemContainer, false) :
                                        getLayoutInflater().inflate(R.layout.item_layout_normal, showItemContainer, false);

        TextView itemText = sharedInvoice ? itemView.findViewById(R.id.itemSelectText) : itemView.findViewById(R.id.itemTextNormal);
        itemText.setText(item.toString());

        if (sharedInvoice) {
            CheckBox checkBox = itemView.findViewById(R.id.selectCheckBox);

            final Item currentItem = item;

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    sendSelectedItem(item, isChecked ? "selectItem": "deselectItem")
            );
        }

        showItemContainer.addView(itemView);
        Toast.makeText(this, "Rechnung wurde erfolgreich geladen.", Toast.LENGTH_SHORT).show();
    }

    private void sendSelectedItem(Item item, String status){
        try {
            if (simpleGattClient.isConnected()) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("toServer", true);
                jsonItem.put("command", status);
                jsonItem.put("item", item.hashCode());

                simpleGattClient.sendData(jsonItem);

            } else {

            }
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onMessageReceived(BluetoothDevice device, JSONObject json) {
        try {
            boolean toServer = json.has("toServer") ? json.getBoolean("toServer") : false;
            boolean toClient = json.has("toClient") ? json.getBoolean("toClient") : false;
            if (toServer) {
                synchronized (InvoiceShowAcitivity.class) {
                    queue.offer(new Pair<>(device, json));
                    if (!isProcessing) {
                        isProcessing = true;
                        processQueue();
                    }
                }
            } else {
                runOnUiThread(() -> Toast.makeText(this, json.toString(), Toast.LENGTH_SHORT).show());
            }
        } catch (JSONException e) {
            runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void processQueue(){
        try {
            while (!queue.isEmpty()) {
                Pair<BluetoothDevice, JSONObject> pair = queue.poll();
                BluetoothDevice device = pair.first;
                JSONObject json = pair.second;

                String command = json.getString("command");
                if (command.equalsIgnoreCase("selectItem")) {
                    int itemCode = json.getInt("item");

                    if (availableItems.containsKey(itemCode)){
                        availableItems.put(itemCode, availableItems.getOrDefault(itemCode, 0) - 1);
                        if (availableItems.get(itemCode) <= 0)
                            availableItems.remove(itemCode);

                        JSONObject anwser = new JSONObject();
                        anwser.put("toClient", true);
                        anwser.put("command", "selectedItem");
                        anwser.put("selectedItem", true);

                        simpleGattServer.sendBroadcastJSON(anwser);
                    } else {
                        // TODO send no message
                    }
                }
            }

            isProcessing = false;

        } catch (JSONException e) {
            runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
package htw.university.sharedbill.controller.invoce;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import htw.university.sharedbill.R;
import htw.university.sharedbill.model.bluetooth.BluetoothLeManager;
import htw.university.sharedbill.model.bluetooth.MessageListener;
import htw.university.sharedbill.model.bluetooth.SimpleGattClient;
import htw.university.sharedbill.model.bluetooth.SimpleGattServer;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.Invoice;
import htw.university.sharedbill.model.invoice.InvoiceObserver;
import htw.university.sharedbill.model.invoice.StorageUtils;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;
import htw.university.sharedbill.model.invoice.Item;


/**
 * Aktivität zur Anzeige einer vollständigen oder gemeinsamen (geteilten) Rechnung.
 * Unterstützt Bluetooth LE-Kommunikation zum Teilen der Rechnung mit anderen Geräten.
 * Ermöglicht Auswahl einzelner Artikel für Teilrechnungen durch Host oder Clients.
 */
public class InvoiceShowActivity extends AppCompatActivity implements MessageListener, InvoiceObserver {

    // UI-Komponenten
    private TextView showTotalPrice, changePaymentStatus, titleGrossPrice;
    private EditText showTotalGrossPrice, showTotalNetPrice, showTotalTaxPrice;
    private EditText showIssuerName, showIssuerStreet, showIssuerZip, showIssuerCity, showIssuerCountry;
    private EditText showInvoiceId, showDate, showVatId, showTransactionId, showCheckSum, showDeviceId, showPaymentMethod;
    private Button confrimInvoice;
    private LinearLayout showItemContainer;

    // Bluetooth-Komponenten
    private SimpleGattServer simpleGattServer;
    private SimpleGattClient simpleGattClient;

    // Datenverwaltung
    List<Item> serverAvailableItems = new ArrayList<>();
    List<Item> clientAvailableItems = new ArrayList<>();

    private final Map<Integer, JSONObject> itemCodeMap = new ConcurrentHashMap<>();
    private final Map<Integer, CheckBox> itemCheckboxMap = new ConcurrentHashMap<>();
    private final Map<Integer, View> itemViewMap = new ConcurrentHashMap<>();

    private EateryInvoice partialInvoiceServer;
    private EateryInvoice partialInvoiceClient;

    private String macAddress;
    private InvoiceWrapper invoiceWrapper;
    private final Object lock = new Object();
    private boolean sharedInvoice = false;

    /**
     * Initialisiert die Aktivität, lädt Rechnung und ggf. Bluetooth-Komponenten.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_invoice);

        // Insets für Systemleisten setzen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.showInvoice), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        invoiceWrapper = (InvoiceWrapper) getIntent().getSerializableExtra("invoice");
        macAddress = getIntent().getStringExtra("mac_address");
        sharedInvoice = getIntent().getBooleanExtra("sharedInvoice", false);

        partialInvoiceServer = new EateryInvoice(); // Erstelle Teilrechnung
        partialInvoiceClient = new EateryInvoice(); // Erstelle Teilrechnung

        if (sharedInvoice)
            initBluetoothLE();

        initViews(); // Initialisiere alle UI-Komponenten
        initListeners(); // Initialisiere alle Listener

        loadInvoiceData(invoiceWrapper, sharedInvoice); // Lade Rechnungsdaten
    }

    /**
     * Initialisiert Bluetooth Low Energy Komponenten.
     */
   private void initBluetoothLE() {
       BluetoothLeManager bleManager = BluetoothLeManager.getInstance(this, getSystemService(BluetoothManager.class));
       simpleGattServer = bleManager.getServer();
       simpleGattClient = bleManager.getClient();
   }

    /**
     * Setzt Listener für Bluetooth-Events und UI-Aktionen.
     */
    private void initListeners() {
        simpleGattServer.registerDataListener(this);
        simpleGattClient.registerMessageListener(this);
        partialInvoiceServer.addObserver(this);
        partialInvoiceClient.addObserver(this);

        confrimInvoice.setOnClickListener(v -> {
            EateryInvoice baseInvoice = (EateryInvoice) invoiceWrapper.getInvoice();
            boolean isHost = macAddress.equalsIgnoreCase("host");

            if ((isHost && serverAvailableItems.isEmpty()) || (!isHost && clientAvailableItems.isEmpty())) {
                EateryInvoice partialInvoice = isHost ? partialInvoiceServer : partialInvoiceClient;

                copyInvoiceData(baseInvoice, partialInvoice, isHost ? "Partial-" : "");

                Toast.makeText(this, "Teilrechnung (" + (isHost ? "Server" : "Client") + ") bestätigt mit " + partialInvoice.getItems().size() + " Artikel(n)", Toast.LENGTH_SHORT).show();

                if (partialInvoice.isValid()) {
                    InvoiceWrapper partialWrapper = new InvoiceWrapper(partialInvoice, "Unbezahlt");

                    if (!InvoiceWrapper.INVOICES.contains(partialWrapper)) {
                        try {
                            StorageUtils.saveInvoiceToAppStorage(this, partialWrapper);
                            InvoiceWrapper.INVOICES.add(partialWrapper);
                        } catch (IOException | JSONException e) {
                            Log.e("StorageUtils", "Rechnung konnte nicht gespeichert werden.");
                        }
                    } else {
                        Toast.makeText(this, "Rechnung existiert bereits.", Toast.LENGTH_SHORT).show();
                    }

                    if (isHost) {
                        Map<String, EateryInvoice> userInvoices = buildUserPartialInvoices();
                        saveUserPartialInvoices(userInvoices, baseInvoice);
                    }

                    startActivity(new Intent(this, InvoiceHistoryActivity.class));
                }
            }
        });
    }

    /**
     * Kopiert relevante Rechnungsdaten vom Ursprungsobjekt zur Teilrechnung.
     */
    private void copyInvoiceData(EateryInvoice source, EateryInvoice target, String idPrefix) {
        target.setIssuer(source.getIssuer());
        target.setInvoiceID(idPrefix + source.getInvoiceID());
        target.setPaymentMethod(source.getPaymentMethod());
        target.setDate(source.getDate());
        target.setVatID(source.getVatID());
        target.setTrnsactionID(source.getTransactionID());
        target.setCheckSum(source.getCheckSum());
        target.setDeviceID(source.getDeviceID());
    }

    /**
     * Erzeugt für jeden Nutzer eine individuelle Teilrechnung basierend auf der Auswahl.
     */
    private Map<String, EateryInvoice> buildUserPartialInvoices() {
        Map<String, EateryInvoice> userInvoices = new HashMap<>();

        for (Map.Entry<Integer, CheckBox> entry : itemCheckboxMap.entrySet()) {
            int code = entry.getKey();
            CheckBox cb = entry.getValue();

            if (cb.isChecked() && !cb.isEnabled()) {
                View itemView = itemViewMap.get(code);
                String user = ((TextView) itemView.findViewById(R.id.itemSelectedBy))
                        .getText().toString().replace("Selected by: ", "").trim();

                try {
                    Item item = Item.fromJSONObject(itemCodeMap.get(code));
                    userInvoices.computeIfAbsent(user, k -> new EateryInvoice()).addItem(item);
                } catch (JSONException e) {
                    Log.e("InvoiceShowController", e.getMessage());
                }
            }
        }

        return userInvoices;
    }

    /**
     * Speichert die erstellten Teilrechnungen im App-Speicher.
     */
    private void saveUserPartialInvoices(Map<String, EateryInvoice> userInvoices, EateryInvoice base) {
        for (Map.Entry<String, EateryInvoice> entry : userInvoices.entrySet()) {
            String user = entry.getKey();
            EateryInvoice invoice = entry.getValue();

            copyInvoiceData(base, invoice, base.getInvoiceID() + "-" + user);

            if (invoice.isValid()) {
                InvoiceWrapper wrapper = new InvoiceWrapper(invoice, "Unbezahlt");
                if (!InvoiceWrapper.INVOICES.contains(wrapper)) {
                    try {
                        StorageUtils.saveInvoiceToAppStorage(this, wrapper);
                        InvoiceWrapper.INVOICES.add(wrapper);
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * Initialisiert alle UI-Komponenten (Textfelder, Buttons, Container).
     */
    private void initViews() {
        // UI-Zuweisung der Views
        showItemContainer = findViewById(R.id.showItemContainer);
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
        titleGrossPrice = findViewById(R.id.textView100);
        confrimInvoice = findViewById(R.id.confrimInvoice);

        if (!sharedInvoice)
            confrimInvoice.setVisibility(View.GONE);
    }

    /**
     * Lädt die Rechnungsdaten in die UI-Felder.
     *
     * @param wrapper Die zu ladende Rechnung
     * @param sharedInvoice Ob es sich um eine gemeinsame Rechnung handelt
     */
    private void loadInvoiceData(InvoiceWrapper wrapper, boolean sharedInvoice) {
        if (wrapper == null) {
            Toast.makeText(this, "Rechnung ist leer oder ungültig.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            final EateryInvoice invoice = (EateryInvoice) wrapper.getInvoice();

            if (!sharedInvoice) {
                // Setze nur Preise, wenn Rechnung nicht geteilt wird
                titleGrossPrice.setText(String.valueOf(invoice.getGrossPrice()));
                showTotalGrossPrice.setText(String.valueOf(invoice.getGrossPrice()));
                showTotalNetPrice.setText(String.valueOf(invoice.getNetPrice()));
                showTotalTaxPrice.setText(String.valueOf(invoice.getTaxPrice()));
            }

            // Setze den Rechnungsersteller
            showIssuerName.setText(invoice.getIssuer().getName());
            showIssuerStreet.setText(invoice.getIssuer().getStreet());
            showIssuerZip.setText(String.valueOf(invoice.getIssuer().getZip()));
            showIssuerCity.setText(invoice.getIssuer().getCity());
            showIssuerCountry.setText(invoice.getIssuer().getCountry());

            //Setze die Rechnungsdetails;
            showInvoiceId.setText(invoice.getInvoiceID());
            showDate.setText(invoice.getDate().toString());
            showVatId.setText(invoice.getVatID());
            showTransactionId.setText(invoice.getTransactionID());
            showCheckSum.setText(invoice.getCheckSum());
            showPaymentMethod.setText(invoice.getPaymentMethod());
            changePaymentStatus.setText(wrapper.getPaymentStatus());
            showDeviceId.setText(invoice.getDeviceID());

            for (Item item : invoice.getItems()) {
                addItem(item, sharedInvoice);
            }

            Toast.makeText(this, "Rechnung wurde erfolgreich geladen.", Toast.LENGTH_SHORT).show();
        } catch (ClassCastException e) {
            Toast.makeText(this, "Fehler: Ungültiger Rechnungstyp.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Fehler beim Laden der Rechnung.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fügt ein einzelnes Item der Ansicht hinzu, inkl. Auswahloption bei geteilten Rechnungen.
     */
    private void addItem(Item item, boolean sharedInvoice) {
        View itemView = getLayoutInflater().inflate(
                sharedInvoice ? R.layout.item_layout_select : R.layout.item_layout_normal,
                showItemContainer,
                false
        );

        TextView itemText = itemView.findViewById(sharedInvoice ? R.id.itemSelectText : R.id.itemTextNormal);
        itemText.setText(item.toString());

        if (sharedInvoice) {
            CheckBox checkBox = itemView.findViewById(R.id.selectCheckBox);
            itemCheckboxMap.put(item.hashCode(), checkBox); // Zuordnung Item(Code) zu CheckBox
            itemViewMap.put(item.hashCode(), itemView); // Zuordnung Item(Code) zu View (SelectedBy TextView)

            try {
                itemCodeMap.put(item.hashCode(), item.getJSONObject()); // Zuordnung Itemcode zu Item
            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sendSelectedItem(item, isChecked ? "selectItem" : "deselectItem"); // Prüfung Auswahl
            });

            if (simpleGattServer.getConnectedDevices().size() != 0) serverAvailableItems.add(item);
            else if (simpleGattClient.isConnected()) clientAvailableItems.add(item);
        }

        showItemContainer.addView(itemView);
    }

    /**
     * Sendet ein Item über Bluetooth, wenn es ausgewählt oder abgewählt wurde.
     */
    private void sendSelectedItem(Item item, String status) {
        if (item == null || status == null || status.isEmpty()) return;

        int itemCode = item.hashCode();
        JSONObject request = new JSONObject();

        try {
            request.put("cmd", status);
            request.put("itemCode", itemCode);

            if (simpleGattClient != null && simpleGattClient.isConnected()) {
                request.put("user", macAddress);
                request.put("toServer", true);
                simpleGattClient.sendData(request);
                return;
            }

            if (simpleGattServer != null && !simpleGattServer.getConnectedDevices().isEmpty()) {
                boolean isSelect = "selectItem".equalsIgnoreCase(status);
                if (isSelect && serverAvailableItems.contains(item)) {
                    serverAvailableItems.remove(item);
                    partialInvoiceServer.addItem(item);
                } else if (!isSelect) {
                    serverAvailableItems.add(item);
                    partialInvoiceServer.removeItem(item);
                } else {
                    return; // Item nicht verfügbar – abbrechen
                }

                runOnUiThread(() -> updateItemUI(itemCode, isSelect, "host"));

                request.put("toClient", true);
                request.put("user", isSelect ? macAddress : "host");
                request.put("selection", true);

                simpleGattServer.sendBroadcastJSON(request);
            }
        } catch (JSONException e) {
            Log.e("InvoiceShowController", e.getMessage());
        }
    }

    /**
     * Verarbeitung empfangener Bluetooth-Nachrichten zur Artikelzuweisung.
     */
    @Override
    public void onMessageReceived(BluetoothDevice device, JSONObject json) {
        synchronized (lock) {
            try {
                String cmd = json.getString("cmd");
                String user = json.getString("user");
                boolean isToServer = json.has("toServer");
                boolean isToClient = json.has("toClient");
                int itemCode = json.getInt("itemCode");

                Item item = Item.fromJSONObject(itemCodeMap.get(itemCode));

                if (isToServer && !simpleGattServer.getConnectedDevices().isEmpty()) {
                    boolean isSelect = "selectItem".equalsIgnoreCase(cmd);
                    boolean available = serverAvailableItems.contains(item);

                    if (isSelect && available) {
                        serverAvailableItems.remove(item);
                    } else if (!isSelect) {
                        serverAvailableItems.add(item);
                    } else {
                        return; // Abbrechen, wenn Item nicht verfügbar
                    }

                    JSONObject response = new JSONObject();
                    response.put("toClient", true);
                    response.put("user", user);
                    response.put("cmd", cmd);
                    response.put("selection", isSelect ? available : true);
                    response.put("itemCode", itemCode);

                    simpleGattServer.sendBroadcastJSON(response);
                    runOnUiThread(() -> updateItemUI(itemCode, isSelect && available, user));

                } else if (isToClient && simpleGattClient.isConnected()) {
                    boolean isSelect = "selectItem".equalsIgnoreCase(cmd);
                    boolean selected = json.getBoolean("selection");

                    if (isSelect && selected) {
                        clientAvailableItems.remove(item);
                        if (macAddress.equalsIgnoreCase(user)) {
                            partialInvoiceClient.addItem(item);
                        }
                    } else if (!isSelect && selected) {
                        clientAvailableItems.add(item);
                        if (macAddress.equalsIgnoreCase(user)) {
                            partialInvoiceClient.removeItem(item);
                        }
                    }

                    runOnUiThread(() -> updateItemUI(itemCode, isSelect && selected, user));
                }

            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }
        }
    }

    /**
     * Aktualisiert die Gesamtpreise, wenn sich die Rechnung ändert.
     */
    @Override
    public void updateInvoice(Invoice invoice) {
        runOnUiThread(() -> {
            showTotalGrossPrice.setText(String.valueOf(invoice.getGrossPrice()));
            showTotalNetPrice.setText(String.valueOf(invoice.getNetPrice()));
            showTotalTaxPrice.setText(String.valueOf(invoice.getTaxPrice()));
            titleGrossPrice.setText(String.valueOf(invoice.getGrossPrice()));
        });
    }


    /**
     * Aktualisiert die UI für ein einzelnes Item (Checkbox, Anzeige des Nutzers).
     */
    private void updateItemUI(int itemCode, boolean selected, String user) {
        CheckBox cb = itemCheckboxMap.get(itemCode);
        View itemView = itemViewMap.get(itemCode);

        if (cb != null) {
            // Checkbox aktualisieren, ohne Listener auszulösen
            cb.setOnCheckedChangeListener(null);
            cb.setChecked(selected);
            cb.setEnabled(!selected || macAddress.equalsIgnoreCase(user));

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    Item item = Item.fromJSONObject(itemCodeMap.get(itemCode));
                    sendSelectedItem(item, isChecked ? "selectItem" : "deselectItem");
                } catch (JSONException e) {
                    Log.e("updateItemUI", "Fehler beim Konvertieren des Items", e);
                }
            });
        }

        if (itemView != null) {
            TextView selectedBy = itemView.findViewById(R.id.itemSelectedBy);
            if (selectedBy != null) {
                selectedBy.setText(selected && user != null && !user.isEmpty()
                        ? "Selected by: " + user
                        : "Selected by:");
            }
        }
    }
}
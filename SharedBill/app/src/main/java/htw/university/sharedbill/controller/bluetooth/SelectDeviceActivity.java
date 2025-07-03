package htw.university.sharedbill.controller.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import htw.university.sharedbill.R;
import htw.university.sharedbill.adapter.DeviceAdapter;
import htw.university.sharedbill.controller.invoce.InvoiceShowActivity;
import htw.university.sharedbill.model.bluetooth.BluetoothLeManager;
import htw.university.sharedbill.model.bluetooth.ConnectionListener;
import htw.university.sharedbill.model.bluetooth.MessageListener;
import htw.university.sharedbill.model.bluetooth.ScanResultListener;
import htw.university.sharedbill.model.bluetooth.SimpleGattClient;
import htw.university.sharedbill.model.bluetooth.SimpleGattServer;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;


/**
 * Diese Activity ermöglicht es, ein Bluetooth-Gerät auszuwählen und eine Rechnung zu übertragen.
 * Sie kann sowohl als Server (Empfänger) als auch als Client (Sender) agieren.
 */
public class SelectDeviceActivity extends AppCompatActivity implements ScanResultListener, ConnectionListener, MessageListener {

    private static final String TAG = "SelectDeviceActivity";

    private static final int REQUEST_CODE_PERMISSIONS = 2;
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private ListView lvDevices;
    private Button btnToggleScan, btnShareInvoice;
    private TextView tvTitle;

    private SimpleGattServer simpleGattServer;
    private SimpleGattClient simpleGattClient;

    private boolean isScanning = false;

    private DeviceAdapter deviceAdapter;
    private final List<BluetoothDevice> foundDevices = new ArrayList<>();
    private String macAddress = null;


    /**
     * Initialisiert die Activity, prüft Berechtigungen und Bluetooth-Zustand.
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        initViews();
        initBluetooth();
        initListeners();
        checkPermissionsAndSetup();
    }

    /**
     * Initialisiert die Benutzeroberfläche und setzt Parameter aus dem Intent.
     */
    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        btnShareInvoice = findViewById(R.id.shareInvoiceViaBT);
        lvDevices = findViewById(R.id.lvDevices);
        btnToggleScan = findViewById(R.id.btnToogleScan);

        deviceAdapter = new DeviceAdapter(this);
        lvDevices.setAdapter(deviceAdapter);

        String title = getIntent().getStringExtra("title");
        boolean disableScan = getIntent().getBooleanExtra("disableScan", false);
        boolean disableShareInvoiceBtn = getIntent().getBooleanExtra("disableShareInvoice", false);

        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        }

        btnToggleScan.setVisibility(disableScan ? View.GONE : View.VISIBLE);
        btnShareInvoice.setVisibility(disableShareInvoiceBtn ? View.GONE : View.VISIBLE);
    }

    /**
     * Initialisiert Bluetooth-Adapter und prüft, ob Bluetooth verfügbar ist.
     */
    private void initBluetooth() {
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = (bluetoothManager != null) ? bluetoothManager.getAdapter() : null;

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_available, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
        }
    }

    /**
     * Fordert den Benutzer auf, Bluetooth zu aktivieren, falls es deaktiviert ist.
     */
    @SuppressLint("MissingPermission")
    private void requestBluetoothEnable() {
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
    }

    /**
     * Prüft Berechtigungen und richtet ggf. Bluetooth-Handler ein.
     */
    private void checkPermissionsAndSetup() {
        if (hasBluetoothPermissions()) {
            setupBluetoothHandlers();
        } else {
            requestBluetoothPermissions();
        }
    }

    /**
     * Prüft, ob alle nötigen Bluetooth-Berechtigungen vorliegen.
     *
     * @return true, wenn alle Berechtigungen vorhanden sind, sonst false
     */
    private boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Fordert fehlende Berechtigungen vom Benutzer an.
     */
    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_ADVERTISE
            }, REQUEST_CODE_PERMISSIONS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSIONS);
        }
    }

    /**
     * Initialisiert GATT-Server oder Client je nach Modus (Server/Client).
     */
    @SuppressLint("MissingPermission")
    private void setupBluetoothHandlers() {
        boolean disableScan = getIntent().getBooleanExtra("disableScan", false);

        if (disableScan) {
            simpleGattServer = BluetoothLeManager.getInstance(this, bluetoothManager).getServer();
            simpleGattServer.registerConnectionListener(this);
            simpleGattServer.registerDataListener(this);
            simpleGattServer.start();
        } else {
            simpleGattClient = BluetoothLeManager.getInstance(this, bluetoothManager).getClient();
            simpleGattClient.registerConnectionListener(this);
            simpleGattClient.registerScanResultListener(this);
            simpleGattClient.registerMessageListener(this);
        }
    }

    /**
     * Initialisiert UI-Listener für Buttons und ListView.
     */
    private void initListeners() {
        btnToggleScan.setOnClickListener(v -> {
            if (simpleGattClient == null) {
                Toast.makeText(this, R.string.bluetooth_client_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            if (isScanning) {
                btnToggleScan.setText(R.string.scan_start);
                simpleGattClient.stopScan();
            } else {
                btnToggleScan.setText(R.string.scan_stop);
                foundDevices.clear();
                deviceAdapter.clear();
                simpleGattClient.startScan();
            }
            isScanning = !isScanning;
        });

        lvDevices.setOnItemClickListener((adapterView, view, position, id) -> {
            BluetoothDevice selectedDevice = foundDevices.get(position);
            if (simpleGattClient != null) {
                Toast.makeText(this, getString(R.string.connecting_to, selectedDevice.getAddress()), Toast.LENGTH_SHORT).show();
                simpleGattClient.connectToDevice(selectedDevice);
            }
        });

        btnShareInvoice.setOnClickListener(v -> shareInvoice());
    }

    /**
     * Sendet eine Rechnung über Bluetooth an verbundene Geräte.
     */
    private void shareInvoice() {
        try {
            InvoiceWrapper invoiceWrapper = (InvoiceWrapper) getIntent().getSerializableExtra("invoice");
            if (invoiceWrapper == null) {
                Toast.makeText(this, R.string.invoice_not_found, Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject json = new JSONObject();
            json.put("toClient", true);
            json.put("toServer", false);
            json.put("cmd", "loadSharedInvoice");
            json.put("invoice", invoiceWrapper.getJSONObject().toString());

            if (simpleGattServer != null) {
                simpleGattServer.sendBroadcastJSON(json);
            }

            Intent intent = new Intent(SelectDeviceActivity.this, InvoiceShowActivity.class);
            intent.putExtra("sharedInvoice", true);
            intent.putExtra("invoice", (Serializable) invoiceWrapper);
            intent.putExtra("mac_address","host");
            startActivity(intent);
            finish();

        } catch (JSONException e) {
            Log.e(TAG, "Fehler beim Erstellen der JSON für Rechnung", e);
            Toast.makeText(this, R.string.error_sending_invoice, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Aufräumarbeiten bei Beenden der Activity: Entfernt Listener.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (simpleGattClient != null){
            simpleGattClient.unregisterConnectionListener(this);
            simpleGattClient.unregisterMessageListener(this);
            simpleGattClient.unregisterScanResultListener(this);
        }
        if (simpleGattServer != null) {
            simpleGattServer.unregisterConnectionListener(this);
            simpleGattServer.unregisterDataListener(this);
        }
    }

    /**
     * Wird aufgerufen, wenn ein neues Bluetooth-Gerät gefunden wurde.
     *
     * @param device Gefundenes Bluetooth-Gerät
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        runOnUiThread(() -> {
            if (!foundDevices.contains(device)) {
                foundDevices.add(device);
                String name = (device.getName() != null) ? device.getName() : getString(R.string.unknown_device);
                deviceAdapter.add(name + " (" + device.getAddress() + ")");
                deviceAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Wird aufgerufen, wenn der Scan abgeschlossen ist.
     */
    @Override
    public void onScanFinished() {
        runOnUiThread(() -> Toast.makeText(this, R.string.scan_finished, Toast.LENGTH_SHORT).show());
    }

    /**
     * Wird aufgerufen, wenn sich der Verbindungsstatus eines Geräts ändert.
     *
     * @param device Bluetooth-Gerät
     * @param status Verbindungsstatus (z. B. "verbunden", "getrennt")
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionState(BluetoothDevice device, String status) {
        runOnUiThread(() -> {

                if (simpleGattClient != null) {

                    for (int i = 0; i < deviceAdapter.getCount(); i++) {
                        String item = deviceAdapter.getItem(i);
                        if (item != null && item.contains(device.getAddress())) {
                            deviceAdapter.setConnectedIndex(i);
                            Log.d(TAG, "Verbunden mit: " + item);
                            break;
                        }
                    }
                } else if (!foundDevices.contains(device)) {
                    foundDevices.add(device);
                    String name = (device.getName() != null) ? device.getName() : getString(R.string.unknown_device);
                    deviceAdapter.add(name + " (" + device.getAddress() + ")");
                    deviceAdapter.notifyDataSetChanged();
                    Toast.makeText(this, status + ": " + device.getAddress(), Toast.LENGTH_SHORT).show();
                }
        });
    }

    /**
     * Verarbeitet empfangene JSON-Nachrichten über Bluetooth.
     *
     * @param device Absendergerät
     * @param json Empfangene Nachricht als JSON-Objekt
     */
    @Override
    public void onMessageReceived(BluetoothDevice device, JSONObject json) {
        try {
            boolean toClient = json.getBoolean("toClient");

            if (toClient) {
                String cmd = json.optString("cmd");
                if ("loadSharedInvoice".equalsIgnoreCase(cmd)) {
                    String invoiceStr = json.getString("invoice");
                    JSONObject invoiceJson = new JSONObject(invoiceStr);

                    InvoiceWrapper invoiceWrapper = InvoiceWrapper.fromJSONObject(invoiceJson);

                    Intent intent = new Intent(SelectDeviceActivity.this, InvoiceShowActivity.class);
                    intent.putExtra("sharedInvoice", true);
                    intent.putExtra("invoice", (Serializable) invoiceWrapper);
                    intent.putExtra("mac_address", macAddress);
                    startActivity(intent);

                    finish();
                } else if (cmd.equalsIgnoreCase("setMac")) {
                    macAddress = json.getString("mac");
                    Log.d("SimpleGattClient", json.getString("mac"));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Fehler beim Empfang der Nachricht", e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

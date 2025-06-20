package htw.university.bluetoothle;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import htw.university.bluetoothle.model.ConnectionListener;
import htw.university.bluetoothle.model.MessageListener;
import htw.university.bluetoothle.model.ScanResultListener;
import htw.university.bluetoothle.model.SimpleGattClient;
import htw.university.bluetoothle.model.SimpleGattServer;

public class DeviceControlActivity extends AppCompatActivity implements ScanResultListener, ConnectionListener, MessageListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 2;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private ListView lvDevices;
    private Button btnStartScan, btnStopScan, btnSendMessage;

    private SimpleGattServer simpleGattServer;
    private SimpleGattClient simpleGattClient;

    private ArrayAdapter<String> deviceAdapter;
    private final List<BluetoothDevice> foundDevices = new ArrayList<>();

    private static final UUID SERVICE_UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb");

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        lvDevices = findViewById(R.id.lvDevices);
        btnStartScan = findViewById(R.id.btnStartScan);
        btnStopScan = findViewById(R.id.btnStopScan);
        btnSendMessage = findViewById(R.id.sendTestJSON);
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvDevices.setAdapter(deviceAdapter);

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth nicht verfügbar", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        }

        if (!hasPermissions()) {
            requestPermissions();
        }

        simpleGattServer = new SimpleGattServer(this, bluetoothManager);
        simpleGattServer.registerConnectionListener(this);
        simpleGattServer.registerDataListener(this);
        simpleGattServer.start();

        simpleGattClient = new SimpleGattClient(this, bluetoothManager);
        simpleGattClient.registerScanResultListener(this);
        simpleGattClient.registerMessageListener(this);

        btnStartScan.setOnClickListener(v -> {
            foundDevices.clear();
            deviceAdapter.clear();
            simpleGattClient.startScan();
            simpleGattClient.startScan();
        });

        btnStopScan.setOnClickListener(v -> {
            simpleGattClient.stopScan();
        });


        lvDevices.setOnItemClickListener((adapterView, view, i, l) -> {
            BluetoothDevice selectedDevice = foundDevices.get(i);
            Toast.makeText(this, "Verbinde mit: " + selectedDevice.getAddress(), Toast.LENGTH_SHORT).show();
            simpleGattClient.connectToDevice(selectedDevice);
        });

        btnSendMessage.setOnClickListener(v -> {
            if (simpleGattClient.isConnected()) {
                simpleGattClient.sendData("Hi vom Client");
            } else {
                simpleGattServer.sendBroadcast("Hi vom Server");
            }
        });


    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT
            }, REQUEST_CODE_PERMISSIONS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Berechtigungen fehlen", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (simpleGattClient != null) simpleGattClient.disconnect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        runOnUiThread(() -> {
            if (!foundDevices.contains(device)) {
                foundDevices.add(device);
                String name = (device.getName() != null) ? device.getName() : "Unbekanntes Gerät";
                deviceAdapter.add(name + " (" + device.getAddress() + ")");
            }
        });
    }

    @Override
    public void onScanFinished() {
        runOnUiThread(() -> Toast.makeText(this, "Scan abgeschlossen", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionState(BluetoothDevice device, String status) {
        runOnUiThread(() -> Toast.makeText(this, status + ": " + device.getAddress(), Toast.LENGTH_SHORT).show()
        );
    }


    @Override
    public void onMessageReceived(BluetoothDevice device, String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}

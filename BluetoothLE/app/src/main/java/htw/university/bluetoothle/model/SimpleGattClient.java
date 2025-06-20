package htw.university.bluetoothle.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleGattClient {

    private static final String TAG = "SimpleGattClient";

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothManager bluetoothManager;
    private BluetoothGatt bluetoothGatt;

    private final Context context;
    private final Handler handler = new Handler();

    private boolean connected = false;
    private boolean scanning = false;
    private static final long SCAN_PERIOD = 10000;
    private final UUID targetServiceUuid;

    private final List<BluetoothDevice> scannedDevices = new ArrayList<>();
    private final List<ScanResultListener> scanResultListeners = new ArrayList<>();
    private final List<MessageListener> dataListeners = new ArrayList<>();

    private static final UUID DATA_UUID = SimpleGattServer.DATA_UUID;


    public SimpleGattClient(@NonNull Context ctx, @NonNull BluetoothManager bluetoothManager) {
        this.context = ctx;
        this.targetServiceUuid = SimpleGattServer.SERVICE_UUID;
        this.bluetoothManager = bluetoothManager;
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.bluetoothLeScanner = this.bluetoothAdapter.getBluetoothLeScanner();
    }

    public void registerScanResultListener(@Nullable ScanResultListener scanResultListener) {
        if (!scanResultListeners.contains(scanResultListener)) 
            scanResultListeners.add(scanResultListener);
    }

    public void registerMessageListener(@NonNull MessageListener listener) {
        if (!dataListeners.contains(listener))
            dataListeners.add(listener);
    }

    public void unregisterScanResultListener(@NonNull ScanResultListener scanResultListener) {
        scanResultListeners.remove(scanResultListener);
    }

    public void unregisterMessageListener(@NonNull MessageListener listener) {
        dataListeners.remove(listener);
    }

    protected void notifyScanResultListener(@NonNull BluetoothDevice device) {
        scanResultListeners.stream().forEach(listener -> listener.onDeviceFound(device));
    }

    private void notifydataListeners(@NonNull BluetoothDevice device, @NonNull String message) {
        dataListeners.stream().forEach(listener -> listener.onMessageReceived(device, message));
        for (MessageListener listener : dataListeners) {
            listener.onMessageReceived(device, message);
        }
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if (result.getScanRecord() != null && result.getScanRecord().getServiceUuids() != null &&
                    result.getScanRecord().getServiceUuids().contains(new android.os.ParcelUuid(targetServiceUuid))) {

                if (!scannedDevices.contains(device)) {
                    scannedDevices.add(device);
                    notifyScanResultListener(device);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan fehlgeschlagen: " + errorCode);
            stopScan();
            scanResultListeners.stream().forEach(listener -> listener.onScanFinished());
        }
    };

    @SuppressLint("MissingPermission")
    public void startScan() {
        if (scanning) return;

        scannedDevices.clear();
        scanning = true;
        bluetoothLeScanner.startScan(leScanCallback);

        handler.postDelayed(() -> {stopScan();}, SCAN_PERIOD);
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (!scanning) return;

        scanning = false;
        bluetoothLeScanner.stopScan(leScanCallback);

        scanResultListeners.stream().forEach(listener -> listener.onScanFinished());
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    @SuppressLint("MissingPermission")
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(@NonNull BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                connected = true;
                Log.i(TAG, "Verbunden mit GATT Server. Services werden entdeckt...");
                gatt.discoverServices();
            } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                connected = false;
                Log.i(TAG, "Verbindung getrennt");
            }
        }

        @Override
        public void onServicesDiscovered(@NonNull BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services entdeckt");
                BluetoothGattService service = gatt.getService(targetServiceUuid);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(DATA_UUID);
                    if (characteristic != null) {
                        boolean notifyEnabled = gatt.setCharacteristicNotification(characteristic, true);
                        Log.d(TAG, "Notification aktiviert: " + notifyEnabled);
                    } else {
                        Log.w(TAG, "Characteristic nicht gefunden");
                    }
                } else {
                    Log.w(TAG, "Service nicht gefunden");
                }
            }
        }


        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            if (DATA_UUID.equals(characteristic.getUuid())) {
                String message = new String(characteristic.getValue(), StandardCharsets.UTF_8);
                Log.d(TAG, "Nachricht empfangen: " + message);
                notifydataListeners(gatt.getDevice(), message);
            }
        }

    };

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @SuppressLint("MissingPermission")
    public void sendData(@NonNull String message) {
        if (bluetoothGatt == null) {
            Log.w(TAG, "Keine GATT-Verbindung vorhanden");
            return;
        }

        BluetoothGattService service = bluetoothGatt.getService(targetServiceUuid);
        if (service == null) {
            Log.w(TAG, "Service nicht gefunden");
            return;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(DATA_UUID);
        if (characteristic == null) {
            Log.w(TAG, "Characteristic nicht gefunden");
            return;
        }

        characteristic.setValue(message.getBytes(StandardCharsets.UTF_8));
        boolean success = bluetoothGatt.writeCharacteristic(characteristic);
        Log.d(TAG, "Nachricht gesendet: " + message + " (Erfolg: " + success + ")");
    }

    public boolean isConnected() {
        return connected;
    }
}

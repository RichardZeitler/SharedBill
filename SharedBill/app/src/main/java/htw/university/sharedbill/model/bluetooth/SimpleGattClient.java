package htw.university.sharedbill.model.bluetooth;

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

import org.json.JSONException;
import org.json.JSONObject;

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
    private final List<ConnectionListener> connectionListeners = new ArrayList<>();

    private static final UUID DATA_UUID = SimpleGattServer.DATA_UUID;

    private final List<byte[]> writeQueue = new ArrayList<>();
    private boolean isWriting = false;
    private BluetoothGattCharacteristic currentCharacteristic;

    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");



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

    public void registerConnectionListener(@Nullable ConnectionListener connectionListener) {
        if (!connectionListeners.contains(connectionListener))
            connectionListeners.add(connectionListener);
    }

    public void registerMessageListener(@NonNull MessageListener listener) {
        if (!dataListeners.contains(listener))
            dataListeners.add(listener);
    }

    public void unregisterConnectionListener(@NonNull ConnectionListener connectionListener) {
        connectionListeners.remove(connectionListener);
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

    protected void notifyConnectionListener(@NonNull BluetoothDevice device, @NonNull String status) {
        connectionListeners.stream().forEach(listener -> listener.onConnectionState(device, status));
    }

    private void notifydataListeners(@NonNull BluetoothDevice device, @NonNull JSONObject json) {
        dataListeners.stream().forEach(listener -> listener.onMessageReceived(device, json));
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
                notifyConnectionListener(gatt.getDevice(),"Verbunden");
                gatt.discoverServices();
            } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                connected = false;
                Log.i(TAG, "Verbindung getrennt");
                notifyConnectionListener(gatt.getDevice(),"Getrennt");
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


        private StringBuilder incomingMessageBuffer = new StringBuilder();
        private boolean receiving = false;

        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            if (DATA_UUID.equals(characteristic.getUuid())) {
                String chunk = new String(characteristic.getValue(), StandardCharsets.UTF_8);
                Log.d(TAG, "Chunk empfangen: " + chunk);

                if (chunk.equals("<<START>>")) {
                    receiving = true;
                    incomingMessageBuffer.setLength(0);
                    Log.d(TAG, "Nachricht Start erkannt");
                } else if (chunk.equals("<<END>>")) {
                    receiving = false;
                    String fullMessage = incomingMessageBuffer.toString();
                    Log.d(TAG, "Nachricht Ende erkannt, kompletter Inhalt: " + fullMessage);

                    try {
                        JSONObject json = new JSONObject(fullMessage);
                        notifydataListeners(gatt.getDevice(), json);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing Fehler", e);
                    }
                } else if (receiving) {
                    incomingMessageBuffer.append(chunk);
                } else {
                    Log.w(TAG, "Paket empfangen ohne Startsignal - ignoriert: " + chunk);
                }
            }
        }

        // **Wichtig: neuer Callback für sequentielles Schreiben**
        @Override
        public void onCharacteristicWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                isWriting = false;
                writeNextChunk();  // nächsten Chunk senden
            } else {
                Log.e(TAG, "Fehler beim Schreiben der Characteristic: " + status);
                isWriting = false; // Reset, um evtl. Wiederholung zu erlauben
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
    public void sendData(@NonNull JSONObject json) {
        if (bluetoothGatt == null || !connected) {
            Log.w(TAG, "Keine GATT-Verbindung vorhanden");
            return;
        }

        BluetoothGattService service = bluetoothGatt.getService(targetServiceUuid);
        if (service == null) {
            Log.w(TAG, "Service nicht gefunden");
            return;
        }

        currentCharacteristic = service.getCharacteristic(DATA_UUID);
        if (currentCharacteristic == null) {
            Log.w(TAG, "Characteristic nicht gefunden");
            return;
        }

        // Nachricht vorbereiten
        String message = json.toString();
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        final int CHUNK_SIZE = 20;

        writeQueue.clear();

        writeQueue.add("<<START>>".getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < msgBytes.length; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, msgBytes.length);
            byte[] chunk = new byte[end - i];
            System.arraycopy(msgBytes, i, chunk, 0, end - i);
            writeQueue.add(chunk);
        }
        writeQueue.add("<<END>>".getBytes(StandardCharsets.UTF_8));

        isWriting = false;
        writeNextChunk();
    }

    @SuppressLint("MissingPermission")
    private void writeNextChunk() {
        if (isWriting || writeQueue.isEmpty() || currentCharacteristic == null) return;

        byte[] nextChunk = writeQueue.remove(0);
        currentCharacteristic.setValue(nextChunk);
        isWriting = bluetoothGatt.writeCharacteristic(currentCharacteristic);
        Log.d(TAG, "Sende Chunk: " + new String(nextChunk, StandardCharsets.UTF_8));
    }


    public boolean isConnected() {
        return connected;
    }
}

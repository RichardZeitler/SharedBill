package htw.university.sharedbill.model.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SimpleGattServer {

    private static final String TAG = "SimpleGattServer";

    protected static final UUID SERVICE_UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb");
    protected static final UUID DATA_UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb");
    protected static final UUID SERVER_DATA_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeAdvertiser advertiser;
    private BluetoothGattServer gattServer;

    private BluetoothGattCharacteristic data;
    private BluetoothGattDescriptor serverData;

    private final Context context;

    private final List<ConnectionListener> connectionListeners = new ArrayList<>();
    private final List<BluetoothDevice> connectedDevices = new ArrayList<>();
    private final List<MessageListener> dataListeners = new ArrayList<>();

    private final Map<BluetoothDevice, StringBuilder> chunkBuffer = new HashMap<>();

    public SimpleGattServer(@NonNull Context ctx, @NonNull BluetoothManager bluetoothManager) {
        this.context = ctx;
        this.bluetoothManager = bluetoothManager;
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    public void registerConnectionListener(@NonNull ConnectionListener l) {
        if (!connectionListeners.contains(l))
            connectionListeners.add(l);
    }

    public void unregisterConnectionListener(ConnectionListener l) {
        connectionListeners.remove(l);
    }

    public void registerDataListener(@NonNull MessageListener listener) {
        if (!dataListeners.contains(listener))
            dataListeners.add(listener);
    }

    public void unregisterDataListener(MessageListener listener) {
        dataListeners.remove(listener);
    }

    private void notifyDataListeners(@NonNull BluetoothDevice device, @NonNull JSONObject json) {
        for (MessageListener listener : dataListeners) {
            listener.onMessageReceived(device, json);
        }
    }

    @SuppressLint("MissingPermission")
    public void start() {
        gattServer = bluetoothManager.openGattServer(context, gattCallback);
        if (gattServer == null) {
            Log.w(TAG, "GattServer konnte nicht gestartet werden.");
            return;
        }

        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        data = new BluetoothGattCharacteristic(
                DATA_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        serverData = new BluetoothGattDescriptor(SERVER_DATA_UUID, BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);
        data.addDescriptor(serverData);
        service.addCharacteristic(data);
        gattServer.addService(service);

        startAdvertising();

        Log.d(TAG, "GATT‑Server + Advertising gestartet (Service " + SERVICE_UUID + ")");
    }

    @SuppressLint("MissingPermission")
    public void stop() {
        stopAdvertising();
        if (gattServer != null) {
            gattServer.close();
            gattServer = null;
        }
        Log.d(TAG, "GATT‑Server gestoppt.");
    }

    @SuppressLint("MissingPermission")
    private void startAdvertising() {
        if (advertiser == null) {
            Log.w(TAG, "Advertiser fehlt");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();

        advertiser.startAdvertising(settings, data, advertiseCallback);
    }

    @SuppressLint("MissingPermission")
    private void stopAdvertising() {
        if (advertiser != null) {
            advertiser.stopAdvertising(advertiseCallback);
        }
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        public void onStartSuccess(AdvertiseSettings s) {
            Log.d(TAG, "Advertising OK");
        }

        public void onStartFailure(int error) {
            Log.w(TAG, "Adv‑Fehler: " + error);
        }
    };

    @SuppressLint("MissingPermission")
    private void sendBroadcast(@NonNull String msg) {
        if (gattServer == null || data == null) return;

        final int CHUNK_SIZE = 20;

        sendChunk("<<START>>".getBytes(StandardCharsets.UTF_8));

        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < msgBytes.length; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, msgBytes.length);
            byte[] chunk = Arrays.copyOfRange(msgBytes, i, end);
            sendChunk(chunk);
        }

        sendChunk("<<END>>".getBytes(StandardCharsets.UTF_8));
        Log.d(TAG, "Chunked Broadcast gesendet: " + msg);
    }

    @SuppressLint("MissingPermission")
    private void sendChunk(@NonNull byte[] chunkBytes) {
        if (gattServer == null || data == null) return;

        data.setValue(chunkBytes);
        for (BluetoothDevice device : connectedDevices) {
            gattServer.notifyCharacteristicChanged(device, data, false);
        }

        Log.d(TAG, "Chunk gesendet: " + new String(chunkBytes, StandardCharsets.UTF_8));
    }

    @SuppressLint("MissingPermission")
    public void sendBroadcastJSON(@NonNull JSONObject jsonObject) {
        sendBroadcast(jsonObject.toString());
        Log.d(TAG, "Broadcastnachricht gesendet: " + jsonObject.toString());
    }

    private final BluetoothGattServerCallback gattCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice dev, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (!connectedDevices.contains(dev)) {
                    connectedDevices.add(dev);

                    connectionListeners.forEach(l -> l.onConnectionState(dev, "verbunden"));
                    Log.d(TAG, "Verbunden: " + dev.getAddress());
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevices.remove(dev);
                connectionListeners.forEach(l -> l.onConnectionState(dev, "getrennt"));
                Log.d(TAG, "Getrennt: " + dev.getAddress());
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice dev, int reqId, BluetoothGattCharacteristic ch, boolean prepared, boolean responseNeeded, int offset, byte[] value) {
            if (DATA_UUID.equals(ch.getUuid())) {
                String chunk = new String(value, StandardCharsets.UTF_8);
                Log.d(TAG, "Chunk empfangen von " + dev.getAddress() + ": " + chunk);

                if ("<<START>>".equals(chunk)) {
                    chunkBuffer.put(dev, new StringBuilder());
                    Log.d(TAG, "Start der Nachricht empfangen");
                } else if ("<<END>>".equals(chunk)) {
                    StringBuilder buffer = chunkBuffer.get(dev);
                    if (buffer == null) {
                        Log.e(TAG, "Kein Puffer für Gerät: " + dev.getAddress());
                        return;
                    }

                    String message = buffer.toString();
                    chunkBuffer.remove(dev);
                    Log.d(TAG, "Ende empfangen. Nachricht: " + message);

                    try {
                        JSONObject json = new JSONObject(message);
                        String cmd = json.getString("cmd");

                        if (cmd.equalsIgnoreCase("getMac")) {
                            JSONObject response = new JSONObject();
                            response.put("toClient", true);
                            response.put("toServer", false);
                            response.put("cmd", "setMac");
                            response.put("mac", dev.getAddress());

                            sendToClientJSON(dev, response);
                        } else {
                            notifyDataListeners(dev, json);
                        }
                        Log.d(TAG, "Nachricht empfangen von " + dev.getAddress() + ": " + message);
                    } catch (JSONException e) {
                        Log.e(TAG, "Ungültiger JSON empfangen von " + dev.getAddress() + ": " + message, e);
                    }
                } else {
                    StringBuilder buffer = chunkBuffer.get(dev);
                    if (buffer == null) {
                        Log.e(TAG, "Chunk empfangen ohne START von Gerät: " + dev.getAddress());
                        if (responseNeeded && gattServer != null) {
                            gattServer.sendResponse(dev, reqId, BluetoothGatt.GATT_FAILURE, offset, value);
                        }
                        return;
                    }
                    buffer.append(chunk);
                }
            }

            if (responseNeeded && gattServer != null) {
                gattServer.sendResponse(dev, reqId, BluetoothGatt.GATT_SUCCESS, offset, value);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice dev, int reqId, int offset, BluetoothGattCharacteristic ch) {
            if (DATA_UUID.equals(ch.getUuid()) && gattServer != null) {
                gattServer.sendResponse(dev, reqId, BluetoothGatt.GATT_SUCCESS, offset, ch.getValue());
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void sendToClientJSON(@NonNull BluetoothDevice device, @NonNull JSONObject jsonObject) {
        if (gattServer == null || data == null) return;
        if (!connectedDevices.contains(device)) {
            Log.w(TAG, "Gerät nicht verbunden: " + device.getAddress());
            return;
        }

        final int CHUNK_SIZE = 20;
        byte[] start = "<<START>>".getBytes(StandardCharsets.UTF_8);
        byte[] end = "<<END>>".getBytes(StandardCharsets.UTF_8);
        byte[] msgBytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        // Senden der Start-Markierung
        data.setValue(start);
        gattServer.notifyCharacteristicChanged(device, data, false);

        // Chunkweise senden
        for (int i = 0; i < msgBytes.length; i += CHUNK_SIZE) {
            int endIdx = Math.min(i + CHUNK_SIZE, msgBytes.length);
            byte[] chunk = Arrays.copyOfRange(msgBytes, i, endIdx);
            data.setValue(chunk);
            gattServer.notifyCharacteristicChanged(device, data, false);
        }

        // Senden der End-Markierung
        data.setValue(end);
        gattServer.notifyCharacteristicChanged(device, data, false);

        Log.d(TAG, "Direktnachricht an " + device.getAddress() + " gesendet: " + jsonObject.toString());
    }

    public List<BluetoothDevice> getConnectedDevices() {
        return connectedDevices;
    }
}

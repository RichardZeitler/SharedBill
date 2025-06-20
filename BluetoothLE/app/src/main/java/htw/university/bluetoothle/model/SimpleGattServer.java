package htw.university.bluetoothle.model;

import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;

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

    public SimpleGattServer(@NonNull Context ctx,@NonNull BluetoothManager bluetoothManager) {
        this.context = ctx;
        this.bluetoothManager = bluetoothManager;
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    public void registerConnectionListener(@NonNull ConnectionListener l) {
        if (!connectionListeners.contains(l)) 
            connectionListeners.add(l);
    }

    public void registerDataListener(@NonNull MessageListener listener) {
        if (!dataListeners.contains(listener)) 
            dataListeners.add(listener);
    }
    
    public void unregisterConnectionListener(ConnectionListener l){
        connectionListeners.remove(l);
    }

    public void unregisterDataListener(MessageListener listener) {
        dataListeners.remove(listener);
    }
    
    private void notifyDataListeners(@NonNull BluetoothDevice device, @NonNull String data) {
        dataListeners.stream().forEach(dataListener -> dataListener.onMessageReceived(device, data));
    }

    @SuppressLint("MissingPermission")
    public void start() {
        gattServer = bluetoothManager.openGattServer(context, gattCallback);
        
        if (gattServer == null) {
            Log.w(TAG, "GattServer konnte nicht gestartet werden.");
            return;
        }

        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        data = new BluetoothGattCharacteristic(DATA_UUID, BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        serverData = new BluetoothGattDescriptor(SERVER_DATA_UUID, BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);

        data.addDescriptor(serverData);
        service.addCharacteristic(data);

        gattServer.addService(service);

        startAdvertising();

        Log.d(TAG, "GATT‑Server + Advertising gestartet (Service " + SERVICE_UUID + ")");
    }
    
    @SuppressLint("MissingPermission")
    public void sendBroadcast(@NonNull String msg) {
        if (gattServer == null || data == null) return;

        data.setValue(msg.getBytes(StandardCharsets.UTF_8));
        connectedDevices.stream().forEach(device -> gattServer.notifyCharacteristicChanged(device, data, false));

        Log.d(TAG, "Broadcastnachricht gesendet: " + msg);
    }
    

    @SuppressLint("MissingPermission")
    public void stop() {
        stopAdvertising();

        if (gattServer != null) {
            gattServer.close(); gattServer = null;
        }

        Log.d(TAG, "GATT‑Server gestoppt.");
    }

    @SuppressLint("MissingPermission")
    private void startAdvertising() {
        if (advertiser == null) {
            Log.w(TAG,"Advertiser fehlt"); return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true).build();
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        advertiser.startAdvertising(settings, data, advertiseCallback);
    }
   
    @SuppressLint("MissingPermission")
    private void stopAdvertising() {
        if (advertiser!=null)
            advertiser.stopAdvertising(advertiseCallback);
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        public void onStartSuccess(AdvertiseSettings s){
            Log.d(TAG,"Advertising OK");
        }

        public void onStartFailure(int error){
            Log.w(TAG,"Adv‑Fehler: "+error);
        }
    };

    private final BluetoothGattServerCallback gattCallback = new BluetoothGattServerCallback() {
        @Override public void onConnectionStateChange(BluetoothDevice dev,int status,int newState){
            if (newState==BluetoothProfile.STATE_CONNECTED){
                if (!connectedDevices.contains(dev)) {
                    connectedDevices.add(dev);
                    connectionListeners.forEach(l->l.onConnectionState(dev,"verbunden"));
                    Log.d(TAG,"Verbunden: "+dev.getAddress());
                }

            } else if (newState==BluetoothProfile.STATE_DISCONNECTED){
                connectedDevices.remove(dev);
                connectionListeners.forEach(l->l.onConnectionState(dev,"getrennt"));
                Log.d(TAG,"Getrennt: "+dev.getAddress());
            }
        }

        /* Nachricht empfangen */
        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice dev, int reqId, BluetoothGattCharacteristic ch, boolean prepared, boolean responseNeeded, int offset, byte[] value) {
            Log.d("gatttttt", "test");
            if (DATA_UUID.equals(ch.getUuid())) {
                String msg = new String(value, StandardCharsets.UTF_8);
                notifyDataListeners(dev, msg);
                Log.d(TAG, "Nachricht empfangen von " + dev.getAddress() + ": " + msg);
            }

            if (responseNeeded && gattServer != null) {
                gattServer.sendResponse(dev, reqId, BluetoothGatt.GATT_SUCCESS, offset, value);
            }
        }

        @SuppressLint("MissingPermission")
        @Override public void onCharacteristicReadRequest(BluetoothDevice dev, int reqId, int offset, BluetoothGattCharacteristic ch){
            Log.d("gatttttt", "test1");

            if (DATA_UUID.equals(ch.getUuid()) && gattServer!=null) {
                gattServer.sendResponse(dev, reqId, BluetoothGatt.GATT_SUCCESS, offset, ch.getValue());
            }
        }
    };

    public List<BluetoothDevice> getConnectedDevices() {
        return connectedDevices;
    }
}

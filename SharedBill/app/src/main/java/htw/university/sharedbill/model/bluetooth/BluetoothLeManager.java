package htw.university.sharedbill.model.bluetooth;

import android.bluetooth.BluetoothManager;
import android.content.Context;

public class BluetoothLeManager {
    private static volatile BluetoothLeManager instance;
    private final SimpleGattClient client;
    private final SimpleGattServer server;

    private BluetoothLeManager(Context ctx, BluetoothManager bluetoothManager) {
        client = new SimpleGattClient(ctx, bluetoothManager);
        server = new SimpleGattServer(ctx, bluetoothManager);
    }

    public static BluetoothLeManager getInstance(Context context, BluetoothManager bluetoothManager) {
        if (instance == null) {
            synchronized (BluetoothLeManager.class) {
                if (instance == null) {
                    instance = new BluetoothLeManager(context.getApplicationContext(), bluetoothManager);
                }
            }
        }
        return instance;
    }

    public SimpleGattClient getClient() {
        return client;
    }

    public SimpleGattServer getServer() {
        return server;
    }
}

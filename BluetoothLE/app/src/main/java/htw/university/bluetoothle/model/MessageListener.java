package htw.university.bluetoothle.model;

import android.bluetooth.BluetoothDevice;

public interface MessageListener {
    void onMessageReceived(BluetoothDevice device, String message);
}

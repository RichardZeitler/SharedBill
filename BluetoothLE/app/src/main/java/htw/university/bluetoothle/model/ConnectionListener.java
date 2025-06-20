package htw.university.bluetoothle.model;

import android.bluetooth.BluetoothDevice;

public interface ConnectionListener {
    void onConnectionState(BluetoothDevice device, String status);
}

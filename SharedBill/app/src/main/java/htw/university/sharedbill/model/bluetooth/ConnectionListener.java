package htw.university.sharedbill.model.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface ConnectionListener {
    void onConnectionState(BluetoothDevice device, String status);
}

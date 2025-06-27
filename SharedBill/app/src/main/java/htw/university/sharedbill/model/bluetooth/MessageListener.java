package htw.university.sharedbill.model.bluetooth;

import android.bluetooth.BluetoothDevice;

import org.json.JSONObject;

public interface MessageListener {
    void onMessageReceived(BluetoothDevice device, JSONObject json);
}

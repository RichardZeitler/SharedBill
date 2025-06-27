package htw.university.sharedbill.model.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface ScanResultListener {
    void onDeviceFound(BluetoothDevice device);
    void onScanFinished();
}
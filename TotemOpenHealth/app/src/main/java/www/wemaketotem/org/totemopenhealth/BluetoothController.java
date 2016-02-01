package www.wemaketotem.org.totemopenhealth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;

/**
 * Singleton for checking the current bluetooth status of the device.
 * The current activity is needed to check the status.
 */
public class BluetoothController implements Serializable {

    private static final String DEBUG_BLUETOOTH = "Bluetooth";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final BluetoothController mInstance = new BluetoothController();

    /**
     * private constructor
     */
    private BluetoothController() {}

    /**
     * Public accessor for the singleton instance.
     * @return Instance of the controller
     */
    public static BluetoothController getInstance() {
        return mInstance;
    }

    /**
     * checks if ble is supported on the device.
     * If not the app is closed.
     * @param activity current activity of the app.
     * @return true if yes.
     */
    private boolean hasBluetooth(Activity activity) {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity.getApplicationContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        Log.i(DEBUG_BLUETOOTH, "Device supports BLE");
        return true;
    }

    /**
     * Turns bluetooth on if bluetooth is supported
     * @param activity application activity
     */
    public void turnBluetoothOn(Activity activity) {
        checkActivity(activity);
        if(hasBluetooth(activity)) {
            BluetoothManager mBluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Log.i(DEBUG_BLUETOOTH, "Bluetooth is turned off");
                Log.i(DEBUG_BLUETOOTH, "Launch request to enable bluetooth");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            if (mBluetoothAdapter.isEnabled()) {
                Log.i(DEBUG_BLUETOOTH, "Bluetooth is enabled");
            }
        }
    }

    /**
     * Checks if the activity isn't null.
     * Instead of a null pointer a illegalState exception is thrown.
     * @param activity the activity to be checked.
     */
    private void checkActivity(Activity activity) {
        if(activity == null) {
            throw new IllegalStateException("Empty activity sent");
        }
    }
}

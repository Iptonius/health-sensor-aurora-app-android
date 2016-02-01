package www.wemaketotem.org.totemopenhealth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Singleton class for doing the bluetooth tasks.
 */
public class DeviceController {

    private static final String DEBUG = "DeviceController";
    private static DeviceController mInstance = new DeviceController();

    private BluetoothLeService mBluetoothLeService;
    private BluetoothDevice mBluetoothDevice;

    /**
     * private constructor to make it an singleton.
     */
    private DeviceController(){
        mBluetoothLeService = new BluetoothLeService();
    }

    /**
     * Static getter of the instance.
     * @return returns the instance of the controller
     */
    public static DeviceController getInstance() {
        return mInstance;
    }

    /**
     * Starts a connection with given device.
     * @param device The device to be connected.
     * @param activity The main activity of the app.
     */
    public void connectDevice(BluetoothDevice device, Activity activity) {
        mBluetoothDevice = device;
        ServiceConnection mServiceConnection = new ServiceConnectionPlus();
        Intent gattServiceIntent = new Intent(activity.getApplicationContext(), BluetoothLeService.class);
        activity.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Getter for the BluetoothService
     * @return bluetooth service
     */
    public BluetoothLeService getBluetoothLeService() {
        return mBluetoothLeService;
    }

    /**
     * Getter for the current connected device
     * @return the current connected device
     */
    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    /**
     * Searches for the characteristic with the given UUID.
     * @param uuid the UUID of the searched characteristic.
     * @return The characteristic if found else NULL is returned.
     */
    private BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        if(mBluetoothLeService != null && mBluetoothLeService.getSupportedGattServices() != null) {
            List<BluetoothGattService> list = mBluetoothLeService.getSupportedGattServices();
            for(BluetoothGattService service: list) {
                for(BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
                    if(characteristic.getUuid().equals(uuid)) {
                        return characteristic;
                    }
                }
            }
        }
        Log.e(DEBUG, "Could not find the characteristic");
        return null;
    }

    /**
     * Method to write a command to the health patch.
     * @param name the name of the characteristic retrieved from {@link CharacteristicName}.
     * @param message The message to be written. Only the first 20 bytes will be written.
     */
    public void writeCharacteristic(CharacteristicName name, String message) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(UUID.fromString(name.getUUID()));
        if(characteristic != null) {
            byte[] bytes= new byte[20];
            byte[] messageBytes = message.getBytes();
            if(messageBytes.length > 20) {
                throw new IllegalStateException("Message is too long");
            }
            System.arraycopy(messageBytes,0,bytes,0,messageBytes.length);
            Log.d("DeviceController", "sent message = " + Arrays.toString(bytes));
            characteristic.setValue(bytes);
            BluetoothGatt mBluetoothGatt = mBluetoothLeService.getBluetoothGatt();
            if(mBluetoothGatt != null) {
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            else
                Log.e(DEBUG, "mBluetoothGatt is null");
        }
        else
            Log.e(DEBUG, "characteristic not found");
    }

    /**
     * Disconnects the device.
     */
    public void disconnectDevice() {
        mBluetoothLeService.disconnect();
    }

    /**
     * closes the connection
     */
    public void closeConnection() {
        mBluetoothLeService.close();
    }

    /**
     * Creates ServiceConnection
     */
    private class ServiceConnectionPlus implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                throw new IllegalStateException("Bluetooth service could not initialize");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mBluetoothDevice.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    }
}

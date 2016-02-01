package www.wemaketotem.org.totemopenhealth.tablayout;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import www.wemaketotem.org.totemopenhealth.BluetoothController;
import www.wemaketotem.org.totemopenhealth.BluetoothLeService;
import www.wemaketotem.org.totemopenhealth.DeviceController;
import www.wemaketotem.org.totemopenhealth.Observer;
import www.wemaketotem.org.totemopenhealth.R;
import www.wemaketotem.org.totemopenhealth.Subject;

/**
 * Controller class of the Scan fragment.
 * Connections to devices are controlled here.
 */
public class FragmentScan extends Fragment implements Subject{

    private static final String DEBUG_BLUETOOTH = "Bluetooth";
    private static final long SCAN_PERIOD = 10000;
    private ArrayList<Observer> observers = new ArrayList<>();
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private View mView;

    /**
     * Gets a new instance of the scan fragment
     * @return new instance of the scan fragment
     */
    public static FragmentScan newInstance() {
       return new FragmentScan();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_scan, container, false);
    }

    @Nullable
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mView = getView();

        BluetoothController controller = BluetoothController.getInstance();
        controller.turnBluetoothOn(getActivity());
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        setUpButtons();
        setUpListView();
    }

    /**
     * Sets the expandable list view.
     * On click listener is set to connect to the device when clicked.
     */
    private void setUpListView() {
        mLeDeviceListAdapter = new LeDeviceListAdapter(getActivity());
        ListView results = (ListView) mView.findViewById(R.id.result_list_main);
        results.setAdapter(mLeDeviceListAdapter);
        results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;
                DeviceController deviceController = DeviceController.getInstance();
                deviceController.connectDevice(device, getActivity());
                if (scanning) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    scanning = false;
                }
            }
        });
    }

    /**
     * Broadcast receiver of the updates send by BluetoothLeService.
     * Also calls the Observers
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                getActivity().registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
                notifyConnected();
            } else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                getActivity().unregisterReceiver(mGattUpdateReceiver);
                notifyDisconnected();
            }
        }
    };

    /**
     * Set up the scan on and off buttons
     */
    private void setUpButtons(){
        Button scanOn = (Button) mView.findViewById(R.id.button_ble_scan_start);
        Button scanOff = (Button) mView.findViewById(R.id.button_ble_scan_stop);

        scanOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_BLUETOOTH, "Start scanning");
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
            }
        });

        scanOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_BLUETOOTH, "Stop scanning");
                scanLeDevice(false);
            }
        });
    }

    /**
     * Scan for devices if true is given, stop scanning if false is given or the time limit has been reached.
     * @param enable true to starts, false to stop.
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Handler handler = new Handler();
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    getActivity().invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void notifyConnected() {
        for(Observer observer:observers) {
            observer.deviceConnected();
        }
    }

    @Override
    public void notifyDisconnected() {
        for(Observer observer:observers) {
            observer.deviceDisconnected();
        }
    }

    /**
     * Intentent filter of the broadcast updates
     * @return intent filter to be used
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * Adapter for the listview.
     */
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter(Activity activity) {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = activity.getLayoutInflater();
        }

        /**
         * Adds a device to the list view if it starts with totem or Totem
         * @param device Device to be added to the list view
         */
        public void addDevice(BluetoothDevice device) {
            if(device != null) {
                if (!mLeDevices.contains(device)) {
                    if(device.getName() != null) {
                        if (device.getName().startsWith("totem") || device.getName().startsWith("Totem")) {
                            mLeDevices.add(device);
                        }
                    }
                }
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}

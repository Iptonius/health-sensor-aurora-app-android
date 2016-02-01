package www.wemaketotem.org.totemopenhealth.tablayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import www.wemaketotem.org.totemopenhealth.CharacteristicName;
import www.wemaketotem.org.totemopenhealth.DeviceController;
import www.wemaketotem.org.totemopenhealth.Observer;
import www.wemaketotem.org.totemopenhealth.R;

/**
 * Fragment for the Device page.
 */
public class FragmentDevice extends Fragment implements Observer {

    private Switch sLog, sAccelero, sGyro, sTemp;
    private TextView tAccelero, tGyro, tTemp, tSeekBar;
    private Button bDisconnect;
    private DiscreteSeekBar mSeekBar;
    private TextView tName;
    private DeviceController mDeviceController;
    private boolean measuring = false;

    /**
     * Creates a new instance of the fragment
     * @return new instance of Device fragment
     */
    public static FragmentDevice newInstance() {
        return new FragmentDevice();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_device, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDeviceController = DeviceController.getInstance();
        initializeWidgets(view);
        initSwitches();
        initSeekBar();
        initButton();
    }

    /**
     * Initializes all the widgets used in the view
     * @param view main view where the widgets exist
     */
    private void initializeWidgets(View view) {
        tName = (TextView) view.findViewById(R.id.device_device_name);
        tName.setText("not connected");

        bDisconnect = (Button) view.findViewById(R.id.device_disconnect);

        sLog = (Switch) view.findViewById(R.id.switch_log_data);
        sAccelero = (Switch) view.findViewById(R.id.switch_accelero);
        sGyro = (Switch) view.findViewById(R.id.switch_gyro);
        sTemp = (Switch) view.findViewById(R.id.switch_temp);

        tAccelero = (TextView) view.findViewById(R.id.device_text_accelero);
        tGyro = (TextView) view.findViewById(R.id.device_text_gyro);
        tTemp = (TextView) view.findViewById(R.id.device_text_temp);
        tSeekBar = (TextView) view.findViewById(R.id.device_text_seek_bar);

        mSeekBar = (DiscreteSeekBar) view.findViewById(R.id.seek_bar);
    }

    /**
     * Init for the switches.
     * Activates all functions except the log data.
     * Sets also for each switch his listener.
     */
    private void initSwitches() {
        sLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "LDstart");
                    setClickableSwitches(false);
                    measuring = true;
                } else {
                    mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "LDstop");
                    setClickableSwitches(true);
                    measuring = false;
                }
            }
        });
        sGyro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "GDaan");
                } else
                    mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "GDuit");
            }
        });
        sTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "TDaan");
                } else
                    mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "TDuit");
            }
        });
        sAccelero.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "ADaan");
                } else
                    mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "ADuit");
            }
        });
    }

    /**
     * Reset the widgets to the default settings
     */
    private void resetWidgets() {
        sLog.setChecked(false);
        sAccelero.setChecked(true);
        sTemp.setChecked(true);
        sGyro.setChecked(true);
        mSeekBar.setEnabled(true);
    }

    /**
     * Init of the seekbar.
     * It sets the listeners. Only on stop is implemented since this value only matters.
     * Also set the interval of the seekbar to 50
     */
    private void initSeekBar() {
        mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "HZ" + seekBar.getProgress() * 50);
            }
        });
        mSeekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 50;
            }
        });
    }

    /**
     * enable or disable all widgets except the log data.
     * @param status true if enabled, false if disabled.
     */
    private void setClickableSwitches(final boolean status) {
        sAccelero.setClickable(status);
        sGyro.setClickable(status);
        sTemp.setClickable(status);
        mSeekBar.setEnabled(status);
        tAccelero.setEnabled(status);
        tGyro.setEnabled(status);
        tTemp.setEnabled(status);
        tSeekBar.setEnabled(status);
    }

    /**
     * sets the listener of the disconnect button
     */
    private void initButton() {
        bDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (measuring) {
                    Toast.makeText(getContext(), "measurement in progress, can't disconnect", Toast.LENGTH_SHORT).show();
                } else {
                    mDeviceController.disconnectDevice();
                }
            }
        });
    }

    /**
     * Observer method.
     * Sets all widgets to the initial value.
     */
    @Override
    public void deviceConnected() {
        tName.setText(mDeviceController.getBluetoothDevice().getName());
        long time = System.currentTimeMillis();
        Log.d("Time", "time is " + time);
        mDeviceController.writeCharacteristic(CharacteristicName.WRITECHAR, "TS" + time);
        resetWidgets();
    }

    /**
     * Observer method.
     * Resets all widgets values.
     */
    @Override
    public void deviceDisconnected() {
        tName.setText("not connected");
        setClickableSwitches(true);
        mDeviceController.closeConnection();
    }
}

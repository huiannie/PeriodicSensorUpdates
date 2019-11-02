package com.itp226.periodicsensorupdates;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// This activity allows the user to configure the
// settings of the sensor service. The user may:
// (1) turn the service on/off.
// (2) select a type of sensor for testing.
// (3) set a threshold, above the user will get a notificaiton.
// The sensor settings are stored as a SharedPreferences
// so that they are retrievable by the Service and the BroadcastReceivers
// even when the activity itself no longer exists.
public class MainActivity extends AppCompatActivity {
    private static final String myId = MainActivity.class.getSimpleName();

    Intent mServiceIntent;

    // Set a sensor setting to decide whether to run sensor or not
    public static final String PreferenceFile = "PeriodicUpdates";
    public static final String PreferenceKeyIsOn = "IsOn";
    public static final String PreferenceKeySensorType = "Type";
    public static final String PreferenceKeyThreshold = "Threshold";

    ArrayList<Sensor> sensorsList;
    ToggleButton toggleButton;
    TextView statusTextView;
    EditText thresholdEdit;
    TextView typeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServiceIntent = new Intent(MainActivity.this, SensorService.class);

        // TextView to show status
        statusTextView = findViewById(R.id.status);
        statusTextView.setText("No service");

        // ToggleButton to turn on or off the service
        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {
                getSharedPreferences(PreferenceFile, MODE_PRIVATE).edit().putBoolean(PreferenceKeyIsOn, isOn).commit();
                if (isOn) {
                    if (!isServiceRunning()) {
                        startService(mServiceIntent);
                    }
                    // else: already running
                    statusTextView.setText("Service is running. ");
                }
                else {
                    stopService(mServiceIntent);
                    statusTextView.setText("Service is stopped.");
                }
            }
        });

        // EditText to change the threshold value
        // The threshold is updated when the button is pressed.
        thresholdEdit = findViewById(R.id.threshold);
        Button thresholdButton = findViewById(R.id.thresholdButton);
        thresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = thresholdEdit.getText().toString();
                try {
                    float threshold = Float.parseFloat(text);
                    getSharedPreferences(PreferenceFile, MODE_PRIVATE).edit().putFloat(PreferenceKeyThreshold, threshold).commit();
                } catch (Exception e) {}
            }
        });

        // TextView to display the selected sensor type
        typeTextView = findViewById(R.id.selectedType);
        // Provide the user with a list of available sensors on device.
        // Let the user select one. Save the choice as a shared preference
        sensorsList = getSensorTypes();
        RecyclerView recyclerView = findViewById(R.id.sensorTypes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListAdapter listAdapter = new ListAdapter(this, sensorsList);
        listAdapter.setItemClickedListener(new ListAdapter.ItemClickedListener() {
            @Override
            public void onItemClicked(View view, int selected) {
                getSharedPreferences(PreferenceFile, MODE_PRIVATE).edit().putInt(PreferenceKeySensorType, sensorsList.get(selected).getType()).commit();
                typeTextView.setText("Selected " + sensorsList.get(selected).getName() + ".\nRestart service to take effect.");
            }
        });
        recyclerView.setAdapter(listAdapter);
    }


    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (SensorService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Load the settings for display purpose
        boolean state = getSharedPreferences(PreferenceFile, MODE_PRIVATE).getBoolean(PreferenceKeyIsOn, false);
        toggleButton.setChecked(state);
        float threshold = getSharedPreferences(PreferenceFile, MODE_PRIVATE).getFloat(PreferenceKeyThreshold, 0);
        thresholdEdit.setText(String.valueOf(threshold));
        int sensorType = getSharedPreferences(PreferenceFile, MODE_PRIVATE).getInt(PreferenceKeySensorType, 0);
        typeTextView.setText("Default sensorType=" + sensorType);
    }

    @Override
    protected void onDestroy() {
        Log.i(myId, "Activity destroyed.");
        super.onDestroy();
    }


    ArrayList<Sensor> getSensorTypes() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ArrayList<Sensor> sensorsList = new ArrayList<>();
        sensorsList.addAll(sensorManager.getSensorList(Sensor.TYPE_ALL));
        Log.i(myId, "number of sensors: " + sensorsList.size());
        return sensorsList;
    }
}

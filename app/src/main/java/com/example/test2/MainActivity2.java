package com.example.test2;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {

    private DatabaseReference rootDatabaseref;
    private TextView tvL1Status, tvL2Status, tvL3Status, tvSoilMoisture, tvTemperature, tvHumidity, tvPumpStatus, tvPumpTime;
    private SwitchMaterial toggleL1, toggleL2, toggleL3;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvL1Status = findViewById(R.id.tvL1Status);
        tvL2Status = findViewById(R.id.tvL2Status);
        tvL3Status = findViewById(R.id.tvL3Status); // Added tvL3Status
        tvSoilMoisture = findViewById(R.id.tvSoilMoisture);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvPumpStatus = findViewById(R.id.tvPumpStatus);
        tvPumpTime = findViewById(R.id.tvPumpTime); // New TextView for pump time
        toggleL1 = findViewById(R.id.toggleL1); // Restored toggleL1
        toggleL2 = findViewById(R.id.toggleL2);
        toggleL3 = findViewById(R.id.toggleL3); // Added toggleL3
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();

        toggleL1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rootDatabaseref.child("L1").setValue(isChecked ? "0" : "1");
        });

        toggleL2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rootDatabaseref.child("L2").setValue(isChecked ? "0" : "1");
        });

        toggleL3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rootDatabaseref.child("L3").setValue(isChecked ? "0" : "1");
        });

        startDataRefreshTimer();
    }

    private void startDataRefreshTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                rootDatabaseref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        runOnUiThread(() -> updateUI(snapshot));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000); // Refresh every second
    }

    private void updateUI(DataSnapshot snapshot) {
        try {
            String dataL1 = snapshot.child("L1").getValue(String.class);
            String dataL2 = snapshot.child("L2").getValue(String.class);
            String dataL3 = snapshot.child("L3").getValue(String.class); // Added L3
            Long dataSoilMoisture = snapshot.child("SoilMoisture").getValue(Long.class);
            Long dataHumidity = snapshot.child("DHT/humidity").getValue(Long.class);
            Long dataTemperature = snapshot.child("DHT/temperature").getValue(Long.class);

            Log.d(TAG, "L1: " + dataL1);
            Log.d(TAG, "L2: " + dataL2);
            Log.d(TAG, "L3: " + dataL3);
            Log.d(TAG, "SoilMoisture: " + dataSoilMoisture);
            Log.d(TAG, "Humidity: " + dataHumidity);
            Log.d(TAG, "Temperature: " + dataTemperature);

            // Apply fuzzy logic to determine pump status and detailed status
            String pumpStatus = determinePumpStatus(dataSoilMoisture, dataHumidity, dataTemperature);
            String detailedPumpStatus = getDetailedPumpStatus(dataSoilMoisture);

            tvL1Status.setText("L1: " + (dataL1 != null && dataL1.equals("0") ? "On" : "Off"));
            tvL2Status.setText("L2: " + (dataL2 != null && dataL2.equals("0") ? "On" : "Off"));
            tvL3Status.setText("L3: " + (dataL3 != null && dataL3.equals("0") ? "On" : "Off"));
            tvSoilMoisture.setText("Soil Moisture: " + dataSoilMoisture);
            tvHumidity.setText("Humidity: " + dataHumidity);
            tvTemperature.setText("Temperature: " + dataTemperature);
            tvPumpStatus.setText("Pump status: " + detailedPumpStatus);

            // Check pump status and update L1 accordingly
            if (pumpStatus.equals("On") || detailedPumpStatus.equals("Normal") || detailedPumpStatus.equals("Medium")) {
                toggleL1.setChecked(true);
                rootDatabaseref.child("L1").setValue("0"); // Turn L1 on
            } else {
                toggleL1.setChecked(false);
                rootDatabaseref.child("L1").setValue("1"); // Turn L1 off
            }

            // Set pump time based on fuzzy logic
            int pumpTime = getPumpTime(dataSoilMoisture);
            tvPumpTime.setText("Pump time: " + pumpTime + "s");

        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage());
        }
    }


    private String determinePumpStatus(Long dataSoilMoisture, Long dataHumidity, Long dataTemperature) {
        if (dataSoilMoisture != null && dataHumidity != null && dataTemperature != null) {
            int soilMoisture = dataSoilMoisture.intValue();
            if (soilMoisture <= 20) {
                return "On";
            } else {
                return "Off";
            }
        } else {
            return "Off"; // Default to "Off" if data is missing
        }
    }

    private String getDetailedPumpStatus(Long dataSoilMoisture) {
        if (dataSoilMoisture != null) {
            int soilMoisture = dataSoilMoisture.intValue();
            if (soilMoisture <= 20) {
                return "Critical";
            } else if (soilMoisture <= 100) {
                return "Normal";
            } else if (soilMoisture <= 178) {
                return "Medium";
            } else {
                return "Off";
            }
        } else {
            return "Unknown"; // Default to "Unknown" if data is missing
        }
    }

    private int getPumpTime(Long dataSoilMoisture) {
        if (dataSoilMoisture != null) {
            int soilMoisture = dataSoilMoisture.intValue();
            if (soilMoisture <= 20) {
                return 15; // Critical
            } else if (soilMoisture <= 100) {
                return 8; // Normal
            } else if (soilMoisture <= 170) {
                return 5; // Medium
            } else {
                return 0; // Low
            }
        } else {
            return 1; // Default to 1 if data is missing
        }
    }
}

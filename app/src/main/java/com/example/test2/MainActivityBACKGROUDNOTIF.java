package com.example.test2;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivityBACKGROUDNOTIF extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private DatabaseReference rootDatabaseref;
    private TextView tvL1Status, tvL2Status, tvL3Status, tvSoilMoisture, tvTemperature, tvHumidity, tvPumpStatus, tvPumpTime;
    private TextView tvL1DataView, tvL2DataView, tvL3DataView, tvSoilMoistureDataView, tvTemperatureDataView, tvHumidityDataView, tvPumpStatusDataView, tvPumpTimeDataView;
    private SwitchMaterial toggleL2, toggleL3;
    private static final String TAG = "MainActivity";
    private void showNotification(String soilCondition) {
        // Notification channel ID is required for Android Oreo and higher
        String channelId = "channel_id";
        CharSequence channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription("Channel Description");
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Soil Condition Notification")
                .setContentText("Soil Condition: " + soilCondition)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        notificationManager.notify(1, builder.build());
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Request the POST_NOTIFICATIONS permission if targeting Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        Intent serviceIntent = new Intent(this, FuzzyLogicService.class);
        startService(serviceIntent);

        // Initialize your UI elements
        tvL1Status = findViewById(R.id.tvL1Status);
        tvL2Status = findViewById(R.id.tvL2Status);
        tvL3Status = findViewById(R.id.tvL3Status);
        tvSoilMoisture = findViewById(R.id.tvSoilMoisture);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvPumpStatus = findViewById(R.id.tvPumpStatus);
        tvPumpTime = findViewById(R.id.tvPumpTime);
        tvPumpTimeDataView = findViewById(R.id.tvPumpTimeDataView);
        tvL1DataView = findViewById(R.id.tvL1DataView);
        tvL2DataView = findViewById(R.id.tvL2DataView);
        tvL3DataView = findViewById(R.id.tvL3DataView);
        tvSoilMoistureDataView = findViewById(R.id.tvSoilMoistureDataView);
        tvTemperatureDataView = findViewById(R.id.tvTemperatureDataView);
        tvHumidityDataView = findViewById(R.id.tvHumidityDataView);
        tvPumpStatusDataView = findViewById(R.id.tvPumpStatusDataView);

        toggleL2 = findViewById(R.id.toggleL2);
        toggleL3 = findViewById(R.id.toggleL3);

        rootDatabaseref = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase references
        DatabaseReference L1StatusRef = rootDatabaseref.child("L1");
        DatabaseReference L2StatusRef = rootDatabaseref.child("L2");
        DatabaseReference L3StatusRef = rootDatabaseref.child("L3");
        DatabaseReference SoilMoistureRef = rootDatabaseref.child("SoilMoisture");
        DatabaseReference TemperatureRef = rootDatabaseref.child("DHT/temperature");
        DatabaseReference HumidityRef = rootDatabaseref.child("DHT/humidity");

        L1StatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.getValue(String.class);
                if (data != null && data.equals("0")) {
                    tvL1DataView.setText("On");
                } else {
                    tvL1DataView.setText("Off");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });


        L2StatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.getValue(String.class);
                if (data != null && data.equals("0")) {
                    tvL2DataView.setText("On");
                } else {
                    tvL2DataView.setText("Off");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        L3StatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.getValue(String.class);
                if (data != null && data.equals("0")) {
                    tvL3DataView.setText("On");
                } else {
                    tvL3DataView.setText("Off");
                }  }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        SoilMoistureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvSoilMoistureDataView.setText(String.valueOf(snapshot.getValue(Long.class)));
                int pumpTime = getPumpTime(snapshot.getValue(Long.class));
                tvPumpTime.setText("Pump time: ");
                tvPumpTimeDataView.setText( pumpTime + "s");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        TemperatureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTemperatureDataView.setText(String.valueOf(snapshot.getValue(Long.class)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        HumidityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvHumidityDataView.setText(String.valueOf(snapshot.getValue(Long.class)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        toggleL2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            L2StatusRef.setValue(isChecked ? "0" : "1");
        });

        toggleL3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            L3StatusRef.setValue(isChecked ? "0" : "1");
        });

        // Schedule regular updates
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> updatePumpStatus());
            }
        }, 0, 1000);
    }

    private void updatePumpStatus() {
        rootDatabaseref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dataL1 = snapshot.child("L1").getValue(String.class);
                String dataL2 = snapshot.child("L2").getValue(String.class);
                Long dataSoilMoisture = snapshot.child("SoilMoisture").getValue(Long.class);                Long dataHumidity = snapshot.child("DHT/humidity").getValue(Long.class);
                Long dataTemperature = snapshot.child("DHT/temperature").getValue(Long.class);

                String pumpStatus = determinePumpStatus(dataL1, dataL2, dataSoilMoisture, dataHumidity, dataTemperature);
                tvPumpStatusDataView.setText(pumpStatus);

                String detailedPumpStatus = getDetailedPumpStatus(dataSoilMoisture);
                tvPumpStatusDataView.setText(detailedPumpStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private String determinePumpStatus(String dataL1, String dataL2, Long dataSoilMoisture, Long dataHumidity, Long dataTemperature) {
        if (dataL2 != null && dataL2.equals("0")) {
            return "Manually On";
        } else if (dataSoilMoisture != null && dataHumidity != null && dataTemperature != null) {
            int soilMoisture = dataSoilMoisture.intValue();
            if (soilMoisture <= 20) {
                return "On";
            } else if (soilMoisture <= 30) {
                return "On";
            } else if (soilMoisture <= 40) {
                return "On";
            } else {
                return "Off";
            }
        } else {
            return "Off";
        }
    }

    private String getDetailedPumpStatus(Long dataSoilMoisture) {
        if (dataSoilMoisture != null) {
            int soilMoisture = dataSoilMoisture.intValue();
            if (soilMoisture <= 20) {
                return "Red Alert !!!";
            } else if (soilMoisture <= 30) {
                return "Yellow Flag";
            } else if (soilMoisture <= 40) {
                return "Green Zone";
            } else {
                return "Optimal Moisture";
            }
        } else {
            return "Unknown";
        }
    }

    private int getPumpTime(Long dataSoilMoisture) {
        if (dataSoilMoisture != null) {
            int soilMoisture = dataSoilMoisture.intValue();
            if (soilMoisture <= 20) {
                return 15;
            } else if (soilMoisture <= 30) {
                return 8;
            } else if (soilMoisture <= 40) {
                return 5;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.e(TAG, "Notification permission denied");
            }
        }
    }
}

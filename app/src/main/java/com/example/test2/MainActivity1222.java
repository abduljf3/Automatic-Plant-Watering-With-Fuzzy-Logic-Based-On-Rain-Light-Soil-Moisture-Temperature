package com.example.test2;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity1222 extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private DatabaseReference rootDatabaseref;
    private TextView tvL1Status, tvL2Status, tvL3Status, tvSoilMoisture, tvTemperature, tvHumidity, tvPumpStatus, tvPumpTime;
    private TextView tvRain,tvRainDataView,tvFanStatus,tvFanDataView,tvL5DataView,tvL11Status,TvL11DataView,tvL4Status,tvL4DataView,tvRecommendation, tvRecommendationDataView, tvSoilCondition, tvSoilConditionDataView, tvL1DataView, tvL2DataView, tvL3DataView, tvSoilMoistureDataView, tvTemperatureDataView, tvHumidityDataView, tvPumpStatusDataView, tvPumpTimeDataView;
    private SwitchMaterial toggleL2, toggleL3, toggleL4,toggleL1;
    TextView resultTextView;
    double currentTime;
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
        // BottomNavigationView setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.Info) {
                Intent infoIntent = new Intent(MainActivity1222.this, InfoActivity.class);
                startActivity(infoIntent);
                return true;
            } else if (itemId == R.id.navigation_analytics) {
                Intent aboutMeIntent = new Intent(MainActivity1222.this, AboutMeActivity.class);
                startActivity(aboutMeIntent);
                return true;
            }else if (itemId == R.id.navigation_settings) {
                finishAffinity(); // Exit the application
                return true;
            }

            return false;
        });

        // Initialize your UI elements
        tvL1Status = findViewById(R.id.tvL1Status);
        tvFanStatus = findViewById(R.id.tvFanStatus);
        tvL2Status = findViewById(R.id.tvL2Status);
        tvL3Status = findViewById(R.id.tvL3Status);
        tvL4Status = findViewById(R.id.tvL4Status);

        tvSoilMoisture = findViewById(R.id.tvSoilMoisture);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvSoilCondition = findViewById(R.id.tvSoilCondition);
        tvRecommendation = findViewById(R.id.tvRecommendation);
        tvRecommendationDataView = findViewById(R.id.tvRecommendationDataView);
        tvRain = findViewById(R.id.tvRain);

        tvL1DataView = findViewById(R.id.tvL1DataView);
        tvFanDataView = findViewById(R.id.tvFanDataView);
        tvRainDataView = findViewById(R.id.tvRainDataView);




        tvSoilMoistureDataView = findViewById(R.id.tvSoilMoistureDataView);
        tvTemperatureDataView = findViewById(R.id.tvTemperatureDataView);
        tvHumidityDataView = findViewById(R.id.tvHumidityDataView);
        tvSoilConditionDataView = findViewById(R.id.tvSoilConditionDataView);

        toggleL2 = findViewById(R.id.toggleL2);
        toggleL3 = findViewById(R.id.toggleL3);
        toggleL4 = findViewById(R.id.toggleL4);

        rootDatabaseref = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase references
        DatabaseReference L1StatusRef = rootDatabaseref.child("L1");
        DatabaseReference L2StatusRef = rootDatabaseref.child("L2");
        DatabaseReference L3StatusRef = rootDatabaseref.child("L3");
        DatabaseReference L4StatusRef = rootDatabaseref.child("L4");
        DatabaseReference L5StatusRef = rootDatabaseref.child("L5");

        DatabaseReference SoilMoistureRef = rootDatabaseref.child("SoilMoisture");
        DatabaseReference TemperatureRef = rootDatabaseref.child("DHT/temperature");
        DatabaseReference HumidityRef = rootDatabaseref.child("DHT/humidity");
        DatabaseReference rainRef = rootDatabaseref.child("rain");

        SoilMoistureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvSoilMoistureDataView.setText(String.valueOf(snapshot.getValue(Long.class)));
                tvRecommendation.setText("Recommendaiton : ");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        rainRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long rainValue = snapshot.getValue(Long.class);
                tvRainDataView.setText(String.valueOf(rainValue));
                Log.d(TAG, "Rain value updated: " + rainValue); // Added log for rain value
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
                String Recommendation = getRecommendation(snapshot.getValue(Long.class));
                tvRecommendation.setText("Recommendation: ");
                tvRecommendationDataView.setText(Recommendation);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        toggleL2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            L1StatusRef.setValue(isChecked ? "0" : "1");
            L5StatusRef.setValue(isChecked ? "0" : "1");

        });

        toggleL3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            L3StatusRef.setValue(isChecked ? "0" : "1");
        });
        toggleL4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            L4StatusRef.setValue(isChecked ? "0" : "1");
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
                String dataL2 = snapshot.child("L5").getValue(String.class);

                String dataL3 = snapshot.child("L2").getValue(String.class);
                String dataL4 = snapshot.child("L6").getValue(String.class);
                Long dataSoilMoisture = snapshot.child("SoilMoisture").getValue(Long.class);
                Long dataHumidity = snapshot.child("DHT/humidity").getValue(Long.class);
                Long dataTemperature = snapshot.child("DHT/temperature").getValue(Long.class);
                Long rain = snapshot.child("rain").getValue(Long.class);

                String pumpStatus = determinePumpStatus(rain, dataL1, dataL2, dataSoilMoisture, dataHumidity, dataTemperature);
                tvL1DataView.setText(pumpStatus);

                String fanStatus = determineFanStatus(dataL3, rain, dataL4, dataSoilMoisture, dataHumidity, dataTemperature);
                tvFanDataView.setText(fanStatus);

                String detailedPumpStatus = getDetailedPumpStatus(rain, dataL1, dataSoilMoisture, dataHumidity, dataTemperature);
                tvSoilConditionDataView.setText(detailedPumpStatus);

                String rainStatus = getRain(rain);
                tvRainDataView.setText(rainStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
    private double applyRules(long soilMoisture, long temperature, long rain) {
        // Soil Moisture Ranges
        double smWet = (soilMoisture <= 500) ? 1 : 0;
        double smNormal = (soilMoisture > 500 && soilMoisture <= 600) ? 1 : 0;
        double smDry = (soilMoisture > 600 && soilMoisture <= 1000) ? 1 : 0;

        // Temperature Ranges
        double tempCold = (temperature <= 20) ? 1 : 0;
        double tempNormal = (temperature > 20 && temperature <= 30) ? 1 : 0;
        double tempHot = (temperature > 30 && temperature <= 55) ? 1 : 0;

        // Rain
        double rainYes = (rain == 1) ? 1 : 0;
        double rainNo = (rain == 0) ? 1 : 0;

        double rule1, rule2, rule3, rule4, rule5, rule6, rule7, rule8, rule9;
        double numerator = 0, denominator = 0;

        if (rain == 1) {
            rule1 = Math.min(Math.min(smWet, tempCold), rainYes) * 0;    // Output = 0
            rule2 = Math.min(Math.min(smWet, tempNormal), rainYes) * 5;  // Output = 5
            rule3 = Math.min(Math.min(smWet, tempHot), rainYes) * 10;    // Output = 10
            rule4 = Math.min(Math.min(smNormal, tempCold), rainYes) * 15; // Output = 15
            rule5 = Math.min(Math.min(smNormal, tempNormal), rainYes) * 20; // Output = 20
            rule6 = Math.min(Math.min(smNormal, tempHot), rainYes) * 25;    // Output = 25
            rule7 = Math.min(Math.min(smDry, tempCold), rainYes) * 30;      // Output = 30
            rule8 = Math.min(Math.min(smDry, tempNormal), rainYes) * 35;    // Output = 35
            rule9 = Math.min(Math.min(smDry, tempHot), rainYes) * 40;       // Output = 40
        } else {
            rule1 = Math.min(Math.min(smWet, tempCold), rainNo) * 45;    // Output = 45
            rule2 = Math.min(Math.min(smWet, tempNormal), rainNo) * 50;  // Output = 50
            rule3 = Math.min(Math.min(smWet, tempHot), rainNo) * 55;     // Output = 55
            rule4 = Math.min(Math.min(smNormal, tempCold), rainNo) * 60; // Output = 60
            rule5 = Math.min(Math.min(smNormal, tempNormal), rainNo) * 65; // Output = 65
            rule6 = Math.min(Math.min(smNormal, tempHot), rainNo) * 70;    // Output = 70
            rule7 = Math.min(Math.min(smDry, tempCold), rainNo) * 75;      // Output = 75
            rule8 = Math.min(Math.min(smDry, tempNormal), rainNo) * 80;    // Output = 80
            rule9 = Math.min(Math.min(smDry, tempHot), rainNo) * 85;       // Output = 85
        }

        // Accumulate the weighted sums for defuzzification
        numerator += rule1 + rule2 + rule3 + rule4 + rule5 + rule6 + rule7 + rule8 + rule9;
        denominator += (rain == 1) ? (Math.min(Math.min(smWet, tempCold), rainYes) +
                Math.min(Math.min(smWet, tempNormal), rainYes) +
                Math.min(Math.min(smWet, tempHot), rainYes) +
                Math.min(Math.min(smNormal, tempCold), rainYes) +
                Math.min(Math.min(smNormal, tempNormal), rainYes) +
                Math.min(Math.min(smNormal, tempHot), rainYes) +
                Math.min(Math.min(smDry, tempCold), rainYes) +
                Math.min(Math.min(smDry, tempNormal), rainYes) +
                Math.min(Math.min(smDry, tempHot), rainYes)) :
                (Math.min(Math.min(smWet, tempCold), rainNo) +
                        Math.min(Math.min(smWet, tempNormal), rainNo) +
                        Math.min(Math.min(smWet, tempHot), rainNo) +
                        Math.min(Math.min(smNormal, tempCold), rainNo) +
                        Math.min(Math.min(smNormal, tempNormal), rainNo) +
                        Math.min(Math.min(smNormal, tempHot), rainNo) +
                        Math.min(Math.min(smDry, tempCold), rainNo) +
                        Math.min(Math.min(smDry, tempNormal), rainNo) +
                        Math.min(Math.min(smDry, tempHot), rainNo));

        return (denominator == 0) ? 0 : numerator / denominator;
    }




    private String determineFanStatus(String dataL2, Long rain, String dataL4, Long dataSoilMoisture, Long dataHumidity, Long dataTemperature) {
        if (dataL4 != null && dataL4.equals("0")) {
            rootDatabaseref.child("L4").setValue("0"); // Turn fan on manually
            return "Manually On";
        } else if (dataSoilMoisture != null && dataHumidity != null && dataTemperature != null) {
            // Check rain value and convert to the needed format
            Long rainValue = (rain != null && rain.equals(0L)) ? 1L : 0L;

            // Apply fuzzy logic rules
            double fuzzyOutput = applyRules(dataSoilMoisture, dataTemperature, rainValue);

            // Debug logs
            Log.d("DEBUG", "Fuzzy Output: " + fuzzyOutput);
            Log.d(TAG, "Calculating pump status with values - Soil Moisture: " + dataSoilMoisture + ", Temperature: " + dataTemperature + ", Rain: " + rainValue);
            // Determine fan status based on fuzzy output
            String newL2Value;
            String statusText;

            if (fuzzyOutput >= 85) {
                newL2Value = "0";
                statusText = "Fan On";
            } else if (fuzzyOutput >= 80) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 75) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 70) {
                newL2Value = "0";
                statusText = "Fan On";
            } else if (fuzzyOutput >= 65) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 60) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 55) {
                newL2Value = "0";
                statusText = "Fan On";
            } else if (fuzzyOutput >= 50) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 45) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 40) {
                newL2Value = "0";
                statusText = "Fan On";
            } else if (fuzzyOutput >= 35) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 30) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 25) {
                newL2Value = "0";
                statusText = "Fan On";
            } else if (fuzzyOutput >= 20) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 15) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 10) {
                newL2Value = "0";
                statusText = "Fan On";
            } else if (fuzzyOutput >= 5) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else if (fuzzyOutput >= 0) {
                newL2Value = "1";
                statusText = "Fan Off";
            } else {
                newL2Value = "1123";
                statusText = "Pump Status Error";
            }

            // Update the fan status in the database if changed
            if (!newL2Value.equals(dataL2)) {
                rootDatabaseref.child("L2").setValue(newL2Value);
            }

            return statusText;
        } else {
            if (!"1".equals(dataL2)) {
                rootDatabaseref.child("L2").setValue("1"); // Turn fan off
            }
            return "Pump Off";
        }
    }





    private String determinePumpStatus(Long rain, String dataL1, String dataL2, Long dataSoilMoisture, Long dataHumidity, Long dataTemperature) {
        if (dataL2 != null && dataL2.equals("0")) {
            rootDatabaseref.child("L5").setValue("0"); // Turn pump on manually
            return "Manually On";
        } else if (rain != null && dataSoilMoisture != null && dataHumidity != null && dataTemperature != null) {
            // Check rain value and convert to the needed format
            Long rainValue = (rain != null && rain.equals(0L)) ? 1L : 0L;

            // Apply fuzzy logic rules
            double fuzzyOutput = applyRules(dataSoilMoisture, dataTemperature, rainValue);

            // Debug logs
            Log.d("DEBUG", "Fuzzy Output: " + fuzzyOutput);
            Log.d(TAG, "Calculating pump status with values - Soil Moisture: " + dataSoilMoisture + ", Temperature: " + dataTemperature + ", Rain: " + rainValue);

            // Determine fan status based on fuzzy output
            String newL2Value;
            String statusText;

            if (fuzzyOutput >= 85) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 80) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 75) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 70) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 65) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 60) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 55) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 50) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 45) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 40) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 35) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 30) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 25) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 20) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 15) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 10) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 5) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 0) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else {
                newL2Value = "1123";
                statusText = "Pump Status Error";
            }

            // Update the fan status in the database if changed
            if (!newL2Value.equals(dataL1)) {
                rootDatabaseref.child("L1").setValue(newL2Value);
            }

            return statusText;
        } else {
            if (!"1".equals(dataL1)) {
                rootDatabaseref.child("L2").setValue("1"); // Turn fan off
            }
            return "Pump Off";
        }
    }

    private String getRain(Long rain) {
        if (rain != null) {
            if (rain == 0) {

                return "Raining";
            } else if (rain == 1) {
                return "Not Raining";
            }
        }
        return "";
    }

    private String getRecommendation(Long dataHumidity) {
        if (dataHumidity != null) {
            if (dataHumidity >= 80 && dataHumidity <= 100) {
                return "Fern";
            } else if (dataHumidity >= 60 && dataHumidity < 80) {
                return "Moss";
            } else if (dataHumidity >= 40 && dataHumidity < 60) {
                return "Orchid";
            } else if (dataHumidity >= 20 && dataHumidity < 40) {
                return "Bromeliad";
            } else if (dataHumidity > 0 && dataHumidity < 20) {
                return "Cactus";
            } else {
                return "Unknown";
            }
        } else {
            return "Unknown";
        }
    }



    private String getDetailedPumpStatus(Long rain, String dataL1, Long dataSoilMoisture, Long dataHumidity, Long dataTemperature) {
        if (rain != null && dataSoilMoisture != null && dataHumidity != null && dataTemperature != null) {
            // Check rain value and convert to the needed format
            Long rainValue = (rain != null && rain.equals(0L)) ? 1L : 0L;

            // Apply fuzzy logic rules
            double fuzzyOutput = applyRules(dataSoilMoisture, dataTemperature, rainValue);

            // Debug logs
            Log.d("DEBUG", "Fuzzy Output: " + fuzzyOutput);
            Log.d(TAG, "Calculating pump status with values - Soil Moisture: " + dataSoilMoisture + ", Temperature: " + dataTemperature + ", Rain: " + rainValue);

            // Determine fan status based on fuzzy output

            if (fuzzyOutput >= 85) return "Good";
            else if (fuzzyOutput >= 80) return "Good";
            else if (fuzzyOutput >= 75) return "Good";
            else if (fuzzyOutput >= 70) return "Good";
            else if (fuzzyOutput >= 65) return "Good";
            else if (fuzzyOutput >= 60) return "Good";
            else if (fuzzyOutput >= 55) return "Good";
            else if (fuzzyOutput >= 50) return "Good";
            else if (fuzzyOutput >= 45) return "Good";
            else if (fuzzyOutput >= 40) return "Very Bad";
            else if (fuzzyOutput >= 35) return "Bad";
            else if (fuzzyOutput >= 30) return "Bad";
            else if (fuzzyOutput >= 25) return "Bad";
            else if (fuzzyOutput >= 20) return "Normal";
            else if (fuzzyOutput >= 15) return "Good";
            else if (fuzzyOutput >= 10) return "Bad";
            else if (fuzzyOutput >= 5) return "Normal";
            else if (fuzzyOutput >= 0) return "Good";



            return "On" ;
        } else {

            return "Off10";
        }
    }


    private String Recommendation(Long dataSoilMoisture) {
        if (dataSoilMoisture != null) {
            int soilMoisture = dataSoilMoisture.intValue();
            if (soilMoisture <= 20) {
                tvPumpStatusDataView.setText("Red Alert !!!");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvPumpStatusDataView.setText("Take immediate action!");
                    }
                }, 1000); // 1000 ms delay
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.e(TAG, "Notification permission denied");
            }

}}}
package com.example.test2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private DatabaseReference rootDatabaseref;
    private TextView tvRainDataView;
    private TextView tvFanDataView;
    private TextView tvRecommendation;
    private TextView tvRecommendationDataView;
    private TextView tvSoilConditionDataView;
    private TextView tvL1DataView;
    private TextView tvSoilMoistureDataView;
    private TextView tvTemperatureDataView;
    private TextView tvHumidityDataView;
    private static final String TAG = "MainActivity";
    private List<String> recommendations;
    private int recommendationIndex = 0;
    private Timer recommendationTimer;

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
                Intent infoIntent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(infoIntent);
                return true;
            } else if (itemId == R.id.navigation_analytics) {
                Intent aboutMeIntent = new Intent(MainActivity.this, AboutMeActivity.class);
                startActivity(aboutMeIntent);
                return true;
            }else if (itemId == R.id.navigation_settings) {
                finishAffinity(); // Exit the application
                return true;
            }

            return false;
        });

        // Initialize your UI elements

        tvRecommendation = findViewById(R.id.tvRecommendation);
        tvRecommendationDataView = findViewById(R.id.tvRecommendationDataView);

        tvL1DataView = findViewById(R.id.tvL1DataView);
        tvFanDataView = findViewById(R.id.tvFanDataView);
        tvRainDataView = findViewById(R.id.tvRainDataView);

        tvSoilMoistureDataView = findViewById(R.id.tvSoilMoistureDataView);
        tvTemperatureDataView = findViewById(R.id.tvTemperatureDataView);
        tvHumidityDataView = findViewById(R.id.tvHumidityDataView);
        tvSoilConditionDataView = findViewById(R.id.tvSoilConditionDataView);




        SwitchMaterial toggleL2 = findViewById(R.id.toggleL2);
        SwitchMaterial toggleL3 = findViewById(R.id.toggleL3);
        SwitchMaterial toggleL4 = findViewById(R.id.toggleL4);

        rootDatabaseref = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase references
        DatabaseReference L1StatusRef = rootDatabaseref.child("L1");
        DatabaseReference L3StatusRef = rootDatabaseref.child("L3");
        DatabaseReference L13StatusRef = rootDatabaseref.child("L13");

        DatabaseReference L4StatusRef = rootDatabaseref.child("L4");
        DatabaseReference L5StatusRef = rootDatabaseref.child("L5");
        DatabaseReference L6StatusRef = rootDatabaseref.child("L6");

        DatabaseReference SoilMoistureRef = rootDatabaseref.child("SoilMoisture");
        DatabaseReference TemperatureRef = rootDatabaseref.child("DHT/temperature");
        DatabaseReference HumidityRef = rootDatabaseref.child("DHT/humidity");
        DatabaseReference rainRef = rootDatabaseref.child("rain");

        SoilMoistureRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
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
                Long humidityValue = snapshot.getValue(Long.class);
                tvHumidityDataView.setText(String.valueOf(humidityValue));
                updateRecommendations(humidityValue);
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
        toggleL4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            L4StatusRef.setValue(isChecked ? "0" : "1");
            L6StatusRef.setValue(isChecked ? "0" : "1");
         /*   L4StatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String status = snapshot.getValue(String.class);
                    if ("0".equals(status)) {
                        toggleL4.setThumbTintList(ContextCompat.getColorStateList(buttonView.getContext(), R.color.primaryColor));
                        toggleL4.setTrackTintList(ContextCompat.getColorStateList(buttonView.getContext(), R.color.primaryDarkColor));
                    } else {
                        toggleL4.setThumbTintList(ContextCompat.getColorStateList(buttonView.getContext(), R.color.black));
                        toggleL4.setTrackTintList(ContextCompat.getColorStateList(buttonView.getContext(), R.color.teal_700));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle possible errors.
                }
            }); */

        });
        toggleL3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            L3StatusRef.setValue(isChecked ? "0" : "1");
            L13StatusRef.setValue(isChecked ? "0" : "1");

        });  // Schedule regular updates
        new Timer().schedule(new TimerTask() {
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

                String dataL3 = snapshot.child("L4").getValue(String.class);
                String dataL4 = snapshot.child("L6").getValue(String.class);
                Long dataSoilMoisture = snapshot.child("SoilMoisture").getValue(Long.class);
                Long dataHumidity = snapshot.child("DHT/humidity").getValue(Long.class);
                Long dataTemperature = snapshot.child("DHT/temperature").getValue(Long.class);
                Long rain = snapshot.child("rain").getValue(Long.class);

                String pumpStatus = determinePumpStatus(rain, dataL1, dataL2, dataSoilMoisture, dataHumidity, dataTemperature);
                tvL1DataView.setText(pumpStatus);

                String fanStatus = determineFanStatus(dataL3, rain, dataL4, dataSoilMoisture, dataHumidity, dataTemperature);
                tvFanDataView.setText(fanStatus);

                String detailedPumpStatus = getDetailedPumpStatus(rain, dataSoilMoisture, dataHumidity, dataTemperature);
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
        // Define fuzzy membership functions
        double smWet = (soilMoisture <= 500) ? 1 : 0;
        double smNormal = (soilMoisture > 500 && soilMoisture <= 600) ? 1 : 0;
        double smDry = (soilMoisture > 600 && soilMoisture <= 1000) ? 1 : 0;

        double tempCold = (temperature <= 20) ? 1 : 0;
        double tempNormal = (temperature > 20 && temperature <= 30) ? 1 : 0;
        double tempHot = (temperature > 30 && temperature <= 50) ? 1 : 0;

        double rainYes = (rain == 0) ? 1 : 0;
        double rainNo = (rain == 1) ? 1 : 0;

        // Define rule outputs
        double[] ruleOutputs = new double[]{
                0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85

        };

        // Calculate rule strengths
        double[] ruleStrengths = new double[18];
        ruleStrengths[0] = smWet * tempCold * rainNo;
        ruleStrengths[1] = smWet * tempNormal * rainNo;
        ruleStrengths[2] = smWet * tempHot * rainNo;
        ruleStrengths[3] = smNormal * tempCold * rainNo;
        ruleStrengths[4] = smNormal * tempNormal * rainNo;
        ruleStrengths[5] = smNormal * tempHot * rainNo;
        ruleStrengths[6] = smDry * tempCold * rainNo;
        ruleStrengths[7] = smDry * tempNormal * rainNo;
        ruleStrengths[8] = smDry * tempHot * rainNo;
        ruleStrengths[9] = smWet * tempCold * rainYes;
        ruleStrengths[10] = smWet * tempNormal * rainYes;
        ruleStrengths[11] = smWet * tempHot * rainYes;
        ruleStrengths[12] = smNormal * tempCold * rainYes;
        ruleStrengths[13] = smNormal * tempNormal * rainYes;
        ruleStrengths[14] = smNormal * tempHot * rainYes;
        ruleStrengths[15] = smDry * tempCold * rainYes;
        ruleStrengths[16] = smDry * tempNormal * rainYes;
        ruleStrengths[17] = smDry * tempHot * rainYes;

        // Find the rule with the highest strength
        double maxStrength = 0;
        double fuzzyOutput = 0;
        for (int i = 0; i < ruleStrengths.length; i++) {
            if (ruleStrengths[i] > maxStrength) {
                maxStrength = ruleStrengths[i];
                fuzzyOutput = ruleOutputs[i];
            }
        }

        return fuzzyOutput;
    }









    private String determineFanStatus(String dataL3, Long rain, String dataL4, Long dataSoilMoisture, Long dataHumidity, Long dataTemperature) {
        if (dataL4 != null && dataL4.equals("0")) {
            rootDatabaseref.child("L4").setValue("0"); // Turn fan on manually

            return "Manually On";
        } else if (dataSoilMoisture != null && dataHumidity != null && dataTemperature != null) {
            // Check rain value and convert to the needed format
            long rainValue = (rain != null && rain.equals(0L)) ? 1L : 0L;

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
                newL2Value = "0";
                statusText = "Fan On";
            } else if (fuzzyOutput >= 15) {
                newL2Value = "0";
                statusText = "Fan On";
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
            if (!newL2Value.equals(dataL3)) {
                rootDatabaseref.child("L4").setValue(newL2Value);
            }

            return statusText;
        } else {
            if (!"1".equals(dataL4)) {
                rootDatabaseref.child("L4").setValue("1"); // Turn fan off
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
            long rainValue = ( rain.equals(0L)) ? 1L : 0L;

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
                newL2Value = "0";
                statusText = "Pump On";
            } else if (fuzzyOutput >= 35) {
                newL2Value = "0";
                statusText = "Pump On";
            } else if (fuzzyOutput >= 30) {
                newL2Value = "0";
                statusText = "Pump On";
            } else if (fuzzyOutput >= 25) {
                newL2Value = "1";
                statusText = "Pump Off";
            } else if (fuzzyOutput >= 20) {
                newL2Value = "0";
                statusText = "Pump On";
            } else if (fuzzyOutput >= 15) {
                newL2Value = "0";
                statusText = "Pump On";
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
            if (rain == 1) {

                return "Raining";
            } else if (rain == 0) {
                return "Not Raining";
            }
        }
        return "";
    }

    private void updateRecommendations(Long humidity) {
        recommendations = getRecommendations(humidity);
        recommendationIndex = 0;
        if (recommendationTimer != null) {
            recommendationTimer.cancel();
        }
        recommendationTimer = new Timer();
        recommendationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (recommendations != null && !recommendations.isEmpty()) {
                        tvRecommendationDataView.setText(recommendations.get(recommendationIndex));
                        recommendationIndex = (recommendationIndex + 1) % recommendations.size();
                    }
                });
            }
        }, 0, 2000);
    }

    // CHANGE getRecommendations
    private List<String> getRecommendations(Long dataHumidity) {
        List<String> recommendations = new ArrayList<>();
        if (dataHumidity != null) {
            if (dataHumidity >= 80 && dataHumidity <= 100) {
                recommendations.add("Fern");
                recommendations.add("Peace Lily");
                recommendations.add("Calathea");
                recommendations.add("Spider Plant");
                recommendations.add("Boston Fern");
                recommendations.add("Areca Palm");
                recommendations.add("Snake Plant");
                recommendations.add("Chinese Evergreen");
                recommendations.add("Dracaena");
                recommendations.add("Fiddle Leaf Fig");
            } else if (dataHumidity >= 60 && dataHumidity < 80) {
                recommendations.add("Moss");
                recommendations.add("Pothos");
                recommendations.add("Philodendron");
                recommendations.add("ZZ Plant");
                recommendations.add("Jade Plant");
                recommendations.add("Parlor Palm");
                recommendations.add("Bamboo Palm");
                recommendations.add("Rubber Plant");
                recommendations.add("Bird of Paradise");
                recommendations.add("Schefflera");
            } else if (dataHumidity >= 40 && dataHumidity < 60) {
                recommendations.add("Orchid");
                recommendations.add("Anthurium");
                recommendations.add("Bromeliad");
                recommendations.add("Peperomia");
                recommendations.add("Hoya");
                recommendations.add("Croton");
                recommendations.add("Alocasia");
                recommendations.add("Ficus");
                recommendations.add("Dieffenbachia");
                recommendations.add("Ctenanthe");
            } else if (dataHumidity >= 20 && dataHumidity < 40) {
                recommendations.add("Bromeliad");
                recommendations.add("Aloe Vera");
                recommendations.add("Succulents");
                recommendations.add("Echeveria");
                recommendations.add("Sedum");
                recommendations.add("Ponytail Palm");
                recommendations.add("Agave");
                recommendations.add("Haworthia");
                recommendations.add("Crassula");
                recommendations.add("Gasteria");
            } else if (dataHumidity > 0 && dataHumidity < 20) {
                recommendations.add("Cactus");
                recommendations.add("Jade Plant");
                recommendations.add("Aloe Vera");
                recommendations.add("Echeveria");
                recommendations.add("Haworthia");
                recommendations.add("Agave");
                recommendations.add("Barrel Cactus");
                recommendations.add("Christmas Cactus");
                recommendations.add("Prickly Pear");
                recommendations.add("Bunny Ear Cactus");
            } else {
                recommendations.add("Unknown");
            }
        } else {
            recommendations.add("Unknown");
        }
        return recommendations;
    }


    private String getDetailedPumpStatus(Long rain, Long dataSoilMoisture, Long dataHumidity, Long dataTemperature) {
        if (rain != null && dataSoilMoisture != null && dataHumidity != null && dataTemperature != null) {
            // Check rain value and convert to the needed format
            long rainValue = ( rain.equals(0L)) ? 1L : 0L;

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
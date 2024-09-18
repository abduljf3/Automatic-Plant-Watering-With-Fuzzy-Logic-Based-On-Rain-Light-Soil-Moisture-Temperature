package com.example.test2;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class FuzzyLogicService extends Service {
    private static final String TAG = "FuzzyLogicService";
    private static final String CHANNEL_ID = "FuzzyLogicServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private Timer timer;
    private DatabaseReference rootDatabaseref;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Fuzzy Logic Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fuzzy Logic Service")
                .setContentText("Initializing...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        startFuzzyLogicTask();
        return START_STICKY;
    }

    private void startFuzzyLogicTask() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                rootDatabaseref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String dataL1 = snapshot.child("L1").getValue(String.class);
                            Long dataSoilMoisture = snapshot.child("SoilMoisture").getValue(Long.class);
                            Long dataHumidity = snapshot.child("DHT/humidity").getValue(Long.class);
                            Long dataTemperature = snapshot.child("DHT/temperature").getValue(Long.class);

                            String detailedPumpStatus = getDetailedPumpStatus(dataL1, dataSoilMoisture, dataHumidity, dataTemperature);

                            if ("Bad".equals(detailedPumpStatus)) {
                                showPopupNotification(detailedPumpStatus);
                            } else {
                                updateNotification(detailedPumpStatus);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error in onDataChange: " + e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, 6000); // Run every 1 minute
    }

    private void updateNotification(String detailedPumpStatus) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Environment Condition")
                .setContentText(detailedPumpStatus)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void showPopupNotification(String detailedPumpStatus) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Environment Condition")
                .setContentText(detailedPumpStatus)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private double fuzzifySoilMoistureWet(Long soilMoisture) {
        if (soilMoisture == null) return 0;
        int sm = soilMoisture.intValue();
        return (sm <= 500) ? 1.0 : 0.0;
    }

    private double fuzzifySoilMoistureNormal(Long soilMoisture) {
        if (soilMoisture == null) return 0;
        int sm = soilMoisture.intValue();
        return (sm > 500 && sm <= 600) ? 1.0 : 0.0;
    }

    private double fuzzifySoilMoistureDry(Long soilMoisture) {
        if (soilMoisture == null) return 0;
        int sm = soilMoisture.intValue();
        return (sm > 600 && sm <= 750) ? 1.0 : 0.0;
    }

    private double fuzzifyTemperatureCold(Long temperature) {
        if (temperature == null) return 0;
        int temp = temperature.intValue();
        return (temp <= 20) ? 1.0 : 0.0;
    }

    private double fuzzifyTemperatureNormal(Long temperature) {
        if (temperature == null) return 0;
        int temp = temperature.intValue();
        return (temp > 20 && temp <= 30) ? 1.0 : 0.0;
    }

    private double fuzzifyTemperatureHot(Long temperature) {
        if (temperature == null) return 0;
        int temp = temperature.intValue();
        return (temp > 30 && temp <= 35) ? 1.0 : 0.0;
    }

    private double applyRules(Long soilMoisture, Long temperature) {
        double smWet = fuzzifySoilMoistureWet(soilMoisture);
        double smNormal = fuzzifySoilMoistureNormal(soilMoisture);
        double smDry = fuzzifySoilMoistureDry(soilMoisture);

        double tempCold = fuzzifyTemperatureCold(temperature);
        double tempNormal = fuzzifyTemperatureNormal(temperature);
        double tempHot = fuzzifyTemperatureHot(temperature);

        // Rule Application
        double rule1 = Math.min(smWet, tempCold);    // Output = 0
        double rule2 = Math.min(smWet, tempNormal);  // Output = 5
        double rule3 = Math.min(smWet, tempHot);     // Output = 10
        double rule4 = Math.min(smNormal, tempCold); // Output = 15
        double rule5 = Math.min(smNormal, tempNormal); // Output = 20
        double rule6 = Math.min(smNormal, tempHot);    // Output = 25
        double rule7 = Math.min(smDry, tempCold);      // Output = 30
        double rule8 = Math.min(smDry, tempNormal);    // Output = 35
        double rule9 = Math.min(smDry, tempHot);       // Output = 40

        // Defuzzification
        double numerator = (rule1 * 0) + (rule2 * 5) + (rule3 * 10) + (rule4 * 15) + (rule5 * 20) + (rule6 * 25) + (rule7 * 30) + (rule8 * 35) + (rule9 * 40);
        double denominator = rule1 + rule2 + rule3 + rule4 + rule5 + rule6 + rule7 + rule8 + rule9;

        return (denominator == 0) ? 0 : numerator / denominator;
    }

    private String getDetailedPumpStatus(String dataL1, Long dataSoilMoisture, Long dataHumidity, Long dataTemperature) {
        if (dataSoilMoisture != null && dataHumidity != null && dataTemperature != null) {
            double fuzzyOutput = applyRules(dataSoilMoisture, dataTemperature);
            if (fuzzyOutput >= 40) {
                if (!"0".equals(dataL1)) {
                }
                return "Bad";
            } else if (fuzzyOutput >= 35) {
                if (!"0".equals(dataL1)) {
                }
                return "Bad";
            } else if (fuzzyOutput >= 30) {
                if (!"0".equals(dataL1)) {
                }
                return "Normal";
            } else if (fuzzyOutput >= 25) {
                if (!"0".equals(dataL1)) {
                }
                return "Bad";
            } else if (fuzzyOutput >= 20) {
                if (!"0".equals(dataL1)) {
                }
                return "Normal";
            } else if (fuzzyOutput >= 15) {
                if (!"0".equals(dataL1)) {
                }
                return "Good";
            } else if (fuzzyOutput >= 10) {
                if (!"0".equals(dataL1)) {
                }
                return "Bad";
            } else if (fuzzyOutput >= 5) {
                if (!"0".equals(dataL1)) {
                }
                return "Normal";
            } else if (fuzzyOutput >= 0) {
                if (!"0".equals(dataL1)) {
                }
                return "Good";
            } else {
                if (!"1".equals(dataL1)) {
                }
                return "Off9";
            }
        } else {
            if (!"1".equals(dataL1)) {
            }
            return "Off10";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        if (timer != null) {
            timer.cancel();
        }
    }
}

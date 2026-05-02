package com.example.new_chess.activities;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new_chess.R;
import com.example.new_chess.ReminderReceiver;
import com.example.new_chess.firebase.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class StartActivity extends AppCompatActivity {
    FirebaseAuth auth;
    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            scheduleDailyReminder(this);
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ThemeManager.applyTheme(
                findViewById(android.R.id.content),
                ThemeManager.getTheme(this)
        );
        askNotificationPermission();

        createNotificationChannel();


        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

    }



    public void LogIn(View v){
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void SignUp(View v){
        startActivity(new Intent(this, SignUpActivity.class));
    }

    public void playLocal(View v){
        startActivity(new Intent(this, LocalGameActivity.class));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "chess_channel",
                    "Chess Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }
    private void askNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            scheduleDailyReminder(this);
        } else {
            // Directly ask for the permission
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void scheduleDailyReminder(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }


}


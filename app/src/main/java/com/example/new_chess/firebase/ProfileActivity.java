package com.example.new_chess.firebase;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new_chess.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTV, emailTV, statsTV;
    private LinearLayout themeOptions, timerOptions;
    private ImageView themeArrow, timerArrow;
    private Switch timerSwitch;
    private EditText timeInput;

    private boolean themeOpen = false;
    private boolean timerOpen = false;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        usernameTV = findViewById(R.id.usernameTV);
        emailTV = findViewById(R.id.emailTV);
        statsTV = findViewById(R.id.statsTV);

        themeOptions = findViewById(R.id.themeOptions);
        timerOptions = findViewById(R.id.timerOptions);

        themeArrow = findViewById(R.id.themeArrow);
        timerArrow = findViewById(R.id.timerArrow);

        timerSwitch = findViewById(R.id.timerSwitch);
        timeInput = findViewById(R.id.timeInput);

        loadUserData();
        setupThemeSection();
        setupTimerSection();
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        emailTV.setText(email);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user != null) {
                    usernameTV.setText(user.username);

                    int wins = user.wins;
                    int losses = user.losses;
                    int draws = user.draws;

                    statsTV.setText("Wins: " + wins +
                            " | Losses: " + losses +
                            " | Draws: " + draws);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // ---------------- THEME ----------------
    private void setupThemeSection() {

        themeArrow.setOnClickListener(v -> {
            themeOpen = !themeOpen;
            themeOptions.setVisibility(themeOpen ? View.VISIBLE : View.GONE);
            themeArrow.setRotation(themeOpen ? 180 : 0);
        });

        findViewById(R.id.themeDefault).setOnClickListener(v -> saveTheme("default"));
        findViewById(R.id.themeClassicLight).setOnClickListener(v -> saveTheme("light"));
        findViewById(R.id.themeClassicDark).setOnClickListener(v -> saveTheme("dark"));
    }

    private void saveTheme(String theme) {
        prefs.edit().putString("theme", theme).apply();
        Toast.makeText(this, "Theme saved!", Toast.LENGTH_SHORT).show();
    }

    // ---------------- TIMER ----------------
    private void setupTimerSection() {

        timerArrow.setOnClickListener(v -> {
            timerOpen = !timerOpen;
            timerOptions.setVisibility(timerOpen ? View.VISIBLE : View.GONE);
            timerArrow.setRotation(timerOpen ? 180 : 0);
        });

        boolean enabled = prefs.getBoolean("timer_enabled", false);
        int minutes = prefs.getInt("timer_minutes", 5);

        timerSwitch.setChecked(enabled);
        timeInput.setVisibility(enabled ? View.VISIBLE : View.GONE);
        timeInput.setText(String.valueOf(minutes));

        timerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("timer_enabled", isChecked).apply();
            timeInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        timeInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String val = timeInput.getText().toString();
                if (!val.isEmpty()) {
                    prefs.edit().putInt("timer_minutes", Integer.parseInt(val)).apply();
                }
            }
        });
    }
}
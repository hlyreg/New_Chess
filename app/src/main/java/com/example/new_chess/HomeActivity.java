package com.example.new_chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        loadUser();

        }

    private void loadUser(){
        TextView greeting = findViewById(R.id.greetingTV);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef =
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("users")
                        .child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);

                greeting.setText("Welcome " + user.username);

            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

        public void playLocal(View v){
            startActivity(new Intent(this, MainActivity.class));
        }

        public void playOnline(View v){
        //    startActivity(new Intent(this, OnlineLobbyActivity.class));
        }

        public void logout(View v){
            auth.signOut();
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
    }

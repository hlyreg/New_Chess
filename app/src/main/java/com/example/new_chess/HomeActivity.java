package com.example.new_chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    FirebaseAuth auth;
    EditText usernameSearch;
    Button searchButton;
    private EditText usernameInput;
    private Button searchUserButton;
    private Button playFriendButton;

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

        usernameInput = findViewById(R.id.usernameInput);
        searchUserButton = findViewById(R.id.searchUserButton);
        playFriendButton = findViewById(R.id.playFriendButton);

        auth = FirebaseAuth.getInstance();

        loadUser();

        playFriendButton.setOnClickListener(v -> {

            usernameInput.setVisibility(View.VISIBLE);
            searchUserButton.setVisibility(View.VISIBLE);
            usernameInput.requestFocus();

        });

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
            startActivity(new Intent(this, LocalGameActivity.class));
        }

        public void playOnline(View v){
            String username = usernameSearch.getText().toString();
            searchForPlayer(username);
        }

    private void searchForPlayer(String username){

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if(!snapshot.exists()){
                            Toast.makeText(getApplicationContext(),"User not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for(DataSnapshot user : snapshot.getChildren()){

                            String opponentUID = user.getKey();

                            createGame(opponentUID);

                            break;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }

    private void createGame(String opponentUID){

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");

        String gameID = gamesRef.push().getKey();

        Map<String,Object> game = new HashMap<>();

        game.put("white", myUID);
        game.put("black", opponentUID);

        gamesRef.child(gameID).setValue(game);

        startGame(gameID);
    }

    private void startGame(String gameID){

        Intent intent = new Intent(this, OnlineGameActivity.class);
        intent.putExtra("GAME_ID", gameID);
        startActivity(intent);

    }

        public void logout(View v){
            auth.signOut();
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
    }

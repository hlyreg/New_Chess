package com.example.new_chess.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new_chess.R;
import com.example.new_chess.firebase.ProfileActivity;
import com.example.new_chess.firebase.ThemeManager;
import com.example.new_chess.firebase.User;
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
    Button searchButton;
    private boolean isInviteDialogShowing = false;
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

        ThemeManager.applyTheme(
                findViewById(android.R.id.content),
                ThemeManager.getTheme(this)
        );

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

        listenForInvites();
        listenForAcceptedInvites();

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


    public void profile(View v){
        startActivity(new Intent(this, ProfileActivity.class));
    }

    public void logout(View v){
        auth.signOut();
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }

    public void playLocal(View v){
        startActivity(new Intent(this, LocalGameActivity.class));
    }

    public void playOnline(View v){
        String username = usernameInput.getText().toString();
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

                            sendInvite(opponentUID);
                            Toast.makeText(getApplicationContext(),"Invite sent!", Toast.LENGTH_SHORT).show();

                            break;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }

    private void sendInvite(String opponentUID){

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference("invites");

        String inviteID = invitesRef.push().getKey();

        Map<String,Object> invite = new HashMap<>();
        invite.put("status", "pending");
        invite.put("from", myUID);
        invite.put("to", opponentUID);
        invite.put("seen", false);
        invite.put("timestamp", System.currentTimeMillis());

        invitesRef.child(inviteID).setValue(invite);
    }

    private void startGame(String gameID, boolean isWhite){

        Intent intent = new Intent(this, OnlineGameActivity.class);
        intent.putExtra("GAME_ID", gameID);
        intent.putExtra("PLAYER_COLOR", isWhite);
        startActivity(intent);
    }


    private void listenForInvites(){

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference("invites");

        invitesRef.orderByChild("to").equalTo(myUID)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        for(DataSnapshot inviteSnap : snapshot.getChildren()){

                            Long timestamp = inviteSnap.child("timestamp").getValue(Long.class);
                            long now = System.currentTimeMillis();

                            if(timestamp != null && now - timestamp > 30000){
                                expireInvite(inviteSnap.getKey());
                                continue;
                            }

                            String status = inviteSnap.child("status").getValue(String.class);
                            if (!"pending".equals(status)) continue;

                            Boolean seen = inviteSnap.child("seen").getValue(Boolean.class);

                            if(seen == null || !seen){
                                markInviteSeen(inviteSnap.getKey());
                                showInvitePopup(inviteSnap);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void showInvitePopup(DataSnapshot inviteSnap){

        if (isInviteDialogShowing) return; // prevent's multiple invites

        isInviteDialogShowing = true;

        String fromUID = inviteSnap.child("from").getValue(String.class);
        String inviteID = inviteSnap.getKey();

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(fromUID)
                .child("username");

        userRef.get().addOnSuccessListener(snapshot -> {

            String username = snapshot.getValue(String.class);

            new AlertDialog.Builder(this)
                    .setTitle("Game Invite")
                    .setMessage(username + " challenged you!")

                    .setPositiveButton("Accept", (dialog, which) -> {
                        acceptInvite(inviteID, fromUID);
                    })

                    .setNegativeButton("Decline", (dialog, which) -> {
                        declineInvite(inviteID);
                    })

                    .show();
        });
    }

    private void listenForAcceptedInvites(){

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference("invites");

        invitesRef.orderByChild("from").equalTo(myUID)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        for(DataSnapshot inviteSnap : snapshot.getChildren()){

                            String status = inviteSnap.child("status").getValue(String.class);
                            Boolean consumed = inviteSnap.child("consumed").getValue(Boolean.class);
                            String gameID = inviteSnap.child("gameID").getValue(String.class);
                            String inviteID = inviteSnap.getKey();

                            if("accepted".equals(status) && (consumed == null || !consumed)){

                                if(gameID != null){

                                    markInviteConsumed(inviteID);

                                    // prevent multiple launches
                                    if(!isFinishing()){
                                        startGame(gameID, true);  //theoretically the inviter is always white
                                    }
                                }
                            }
                        }
                    }

                    @Override public void onCancelled(DatabaseError error) {}
                });
    }
    private void acceptInvite(String inviteID, String opponentUID){

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        String gameID = rootRef.child("games").push().getKey();

        Map<String,Object> game = new HashMap<>();
        game.put("white", opponentUID);
        game.put("black", myUID);

        rootRef.child("games").child(gameID).setValue(game);

        // store gameID in invite
        rootRef.child("invites").child(inviteID).child("gameID").setValue(gameID);

        rootRef.child("invites").child(inviteID).child("status").setValue("accepted");

        markInviteConsumed(inviteID);
        startGame(gameID, false);
    }
    private void markInviteConsumed(String inviteID){
        FirebaseDatabase.getInstance()
                .getReference("invites")
                .child(inviteID)
                .child("consumed")
                .setValue(true);
    }

    private void declineInvite(String inviteID){
        FirebaseDatabase.getInstance()
                .getReference("invites")
                .child(inviteID)
                .child("status")
                .setValue("declined");
    }

    private void markInviteSeen(String inviteID){
        FirebaseDatabase.getInstance()
                .getReference("invites")
                .child(inviteID)
                .child("seen")
                .setValue(true);
    }

    private void expireInvite(String inviteID){
        FirebaseDatabase.getInstance()
                .getReference("invites")
                .child(inviteID)
                .child("status")
                .setValue("expired");
    }

}




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
import com.example.new_chess.firebase.ThemeManager;
import com.example.new_chess.firebase.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText usernameInput;
    private Button searchUserButton, playFriendButton;

    private boolean isInviteDialogShowing = false;
    private boolean gameStarted = false;

    private DatabaseReference invitesRef;
    private ValueEventListener invitesListener;
    private ValueEventListener acceptedListener;

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

        auth = FirebaseAuth.getInstance();

        usernameInput = findViewById(R.id.usernameInput);
        searchUserButton = findViewById(R.id.searchUserButton);
        playFriendButton = findViewById(R.id.playFriendButton);

        invitesRef = FirebaseDatabase.getInstance().getReference("invites");

        loadUser();

        playFriendButton.setOnClickListener(v -> {
            usernameInput.setVisibility(View.VISIBLE);
            searchUserButton.setVisibility(View.VISIBLE);
            usernameInput.requestFocus();
        });

        listenForInvites();
        listenForAcceptedInvites();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (invitesListener != null)
            invitesRef.removeEventListener(invitesListener);

        if (acceptedListener != null)
            invitesRef.removeEventListener(acceptedListener);
    }

    private void loadUser() {
        TextView greeting = findViewById(R.id.greetingTV);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null)
                    greeting.setText("Welcome " + user.username);
            }

            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    public void profile(View v) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    public void logout(View v) {
        auth.signOut();
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }

    public void playLocal(View v) {
        startActivity(new Intent(this, LocalGameActivity.class));
    }

    public void playOnline(View v) {
        String username = usernameInput.getText().toString();
        searchForPlayer(username);
    }

    private void searchForPlayer(String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot user : snapshot.getChildren()) {
                            String opponentUID = user.getKey();
                            if (opponentUID != null) {
                                sendInvite(opponentUID);
                                Toast.makeText(getApplicationContext(), "Invite sent!", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }

                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void sendInvite(String opponentUID) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String inviteID = invitesRef.push().getKey();

        Map<String, Object> invite = new HashMap<>();
        invite.put("status", "pending");
        invite.put("from", myUID);
        invite.put("to", opponentUID);
        invite.put("seen", false);
        invite.put("timestamp", System.currentTimeMillis());

        invitesRef.child(inviteID).setValue(invite);
    }

    private void startGame(String gameID, boolean isWhite) {
        if (gameStarted) return;
        gameStarted = true;

        Intent intent = new Intent(this, OnlineGameActivity.class);
        intent.putExtra("GAME_ID", gameID);
        intent.putExtra("PLAYER_COLOR", isWhite);
        startActivity(intent);
    }

    private void listenForInvites() {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        invitesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot inviteSnap : snapshot.getChildren()) {

                    Long timestamp = inviteSnap.child("timestamp").getValue(Long.class);
                    long now = System.currentTimeMillis();

                    if (timestamp != null && now - timestamp > 30000) {
                        invitesRef.child(inviteSnap.getKey()).removeValue();
                        continue;
                    }

                    String status = inviteSnap.child("status").getValue(String.class);
                    if (!"pending".equals(status)) continue;

                    Boolean seen = inviteSnap.child("seen").getValue(Boolean.class);

                    if (seen == null || !seen) {
                        markInviteSeen(inviteSnap.getKey());
                        showInvitePopup(inviteSnap);
                    }
                }
            }

            @Override public void onCancelled(DatabaseError error) {}
        };

        invitesRef.orderByChild("to").equalTo(myUID)
                .addValueEventListener(invitesListener);
    }

    private void showInvitePopup(DataSnapshot inviteSnap) {
        if (isInviteDialogShowing) return;

        String fromUID = inviteSnap.child("from").getValue(String.class);
        String inviteID = inviteSnap.getKey();

        if (fromUID == null || inviteID == null) return;

        isInviteDialogShowing = true;

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
                        isInviteDialogShowing = false;
                        acceptInvite(inviteID, fromUID);
                    })

                    .setNegativeButton("Decline", (dialog, which) -> {
                        isInviteDialogShowing = false;
                        declineInvite(inviteID);
                    })

                    .setOnDismissListener(dialog -> isInviteDialogShowing = false)
                    .show();
        });
    }

    private void listenForAcceptedInvites() {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        acceptedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot inviteSnap : snapshot.getChildren()) {

                    String status = inviteSnap.child("status").getValue(String.class);
                    Boolean consumed = inviteSnap.child("consumed").getValue(Boolean.class);
                    String gameID = inviteSnap.child("gameID").getValue(String.class);
                    String inviteID = inviteSnap.getKey();

                    if ("accepted".equals(status) && (consumed == null || !consumed)) {
                        if (gameID != null && inviteID != null) {

                            invitesRef.child(inviteID).child("consumed").setValue(true);

                            startGame(gameID, true);
                        }
                    }
                }
            }

            @Override public void onCancelled(DatabaseError error) {}
        };

        invitesRef.orderByChild("from").equalTo(myUID)
                .addValueEventListener(acceptedListener);
    }

    private void acceptInvite(String inviteID, String opponentUID) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        String gameID = rootRef.child("games").push().getKey();

        Map<String, Object> game = new HashMap<>();
        game.put("white", opponentUID);
        game.put("black", myUID);

        rootRef.child("games").child(gameID).setValue(game);

        rootRef.child("invites").child(inviteID).child("gameID").setValue(gameID);
        rootRef.child("invites").child(inviteID).child("status").setValue("accepted");
        rootRef.child("invites").child(inviteID).child("consumed").setValue(true);

        startGame(gameID, false);
    }

    private void declineInvite(String inviteID) {
        invitesRef.child(inviteID).removeValue();
    }

    private void markInviteSeen(String inviteID) {
        invitesRef.child(inviteID).child("seen").setValue(true);
    }
}
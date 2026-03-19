package com.example.new_chess;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new_chess.game.Board;
import com.example.new_chess.game.GameState;
import com.example.new_chess.game.Move;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;
import com.example.new_chess.pieces.Piece;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jspecify.annotations.NonNull;

public class OnlineGameActivity extends AppCompatActivity {
    private boolean myTurn;
    private ChessBoardView boardView;
    private GameState gameState;
    private DatabaseReference gameRef;
    private boolean gameReady = false;
    private String gameID;
    private boolean amIWhite;
    private String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "onCreate called");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_online_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gameID = getIntent().getStringExtra("GAME_ID");

        if(gameID == null){
            Log.e("GAME", "gameID is NULL!");
            finish();
            return;
        }

        gameRef = FirebaseDatabase
                .getInstance()
                .getReference("games")
                .child(gameID);


        determinePlayerColor();

    }



    private void sendMoveToFirebase(Move move){

        DatabaseReference movesRef = FirebaseDatabase.getInstance()
                .getReference("games")
                .child(gameID)
                .child("moves");

        movesRef.push().setValue(move);
    }

    private void setupFirebaseListener() {
        gameRef.child("lastMove").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Move move = snapshot.getValue(Move.class);

                if (move == null) return;

                // Check if it’s the opponent's move
                if(move.isAmIWhite() == amIWhite) return;

                Piece piece = null;
                if(amIWhite){
                    piece = gameState.getBoard().getPlayer(0).getPieces()[move.getPieceID()];
                }

                else{
                    piece = gameState.getBoard().getPlayer(1).getPieces()[move.getPieceID()];
                }

                if (piece != null) {
                    gameState.makeMove(piece, move.getChange());
                    myTurn = true;
                    boardView.invalidate();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read opponent move", error.toException());
            }
        });
    }

    private void listenForMoves(){

        DatabaseReference movesRef = FirebaseDatabase.getInstance()
                .getReference("games")
                .child(gameID)
                .child("moves");

        movesRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                Move move = snapshot.getValue(Move.class);

                if(move == null) return;

                // Ignore your own move
                if(move.isAmIWhite() == amIWhite) return;

                applyMoveFromFirebase(move);

                myTurn = true;
            }

            @Override public void onChildChanged(DataSnapshot snapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot snapshot) {}
            @Override public void onChildMoved(DataSnapshot snapshot, String s) {}
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void applyMoveFromFirebase(Move move){

        Piece piece = gameState.getBoard().getPieceById(move.getPieceID(), move.isAmIWhite());

        if(piece == null){
            Log.e("MOVE", "Piece not found!");
            return;
        }

        gameState.makeMove(piece, move.getChange());

        myTurn = true;

        boardView.switchTurn();
        boardView.setMyTurn(true);
        boardView.invalidate();
    }

    private void showWinDialog(int loser){

        String message = (loser == 1) ? "White wins!" : "Black wins!";

        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)

                .setCancelable(false)

                .setPositiveButton("Back to Home", (dialog, which) -> {

                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                })

                .show();
    }

    private void determinePlayerColor(){


        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if(isFinishing() || isDestroyed()){
                    return;
                }

                String whiteUID = snapshot.child("white").getValue(String.class);
                String blackUID = snapshot.child("black").getValue(String.class);

                if(whiteUID == null || blackUID == null){
                    Log.e("GAME", "Missing player data");
                    return;
                }

                if(myUID.equals(whiteUID)){
                    amIWhite = true;
                }
                else if(myUID.equals(blackUID)){
                    amIWhite = false;
                }
                else{
                    Log.e("GAME", "Player not part of this game");
                }

                Log.d("GAME", "myUID=" + myUID);
                Log.d("GAME", "whiteUID=" + whiteUID);
                Log.d("GAME", "blackUID=" + blackUID);


                setupGame();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void setupGame(){

        boardView = findViewById(R.id.chessBoard);

        Player white = new Player(0);
        Player black = new Player(1);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //setupFirebaseListener();

        Board board = new Board(white, black);
        gameState = new GameState(new Board(board.getPlayer(0), board.getPlayer(1)));

        boardView.setBoard(gameState.getBoard(), gameState);

        myTurn = amIWhite;
        gameReady = true;

        boardView.setMyTurn(myTurn);

        listenForMoves();

        boardView.setMoveListener((piece, move) -> {

            if(!gameReady) return;
            if(!myTurn) return;

            Move m = new Move(
                    piece.getID(),
                    piece.getPlace(),
                    move,
                    false,
                    amIWhite
            );

            gameState.makeMove(piece, move);

            int isCheckmate = gameState.checkMate();
            if(isCheckmate != -1){
                showWinDialog(isCheckmate);
            }

            sendMoveToFirebase(m);

            myTurn = false;
            boardView.setMyTurn(false);

            boardView.switchTurn();
            boardView.invalidate();
        });

        Log.d("TURN", "amIWhite=" + amIWhite);
        Log.d("TURN", "myTurn=" + myTurn);
    }


}
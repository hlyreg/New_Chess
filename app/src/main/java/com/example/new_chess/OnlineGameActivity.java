package com.example.new_chess;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OnlineGameActivity extends AppCompatActivity {
    private boolean myTurn;
    private ChessBoardView boardView;
    private GameState gameState;
    private DatabaseReference gameRef;
    private boolean amIWhite = true; // later this will come from Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_online_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String gameID = getIntent().getStringExtra("GAME_ID");

        gameRef = FirebaseDatabase
                .getInstance()
                .getReference("games")
                .child(gameID);


        myTurn = amIWhite;

        boardView = findViewById(R.id.chessBoard);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("games").child("game1"); //For now "game1" is just a test game room.
        setupFirebaseListener();

        Player white = new Player(0);
        Player black = new Player(1);

        Board board = new Board(white, black);
        gameState = new GameState(new Board(board.getPlayer(0), board.getPlayer(1)));


        boardView.setMoveListener((piece, move) -> {

            if(!myTurn) return;  // skip if not my turn

            Move m = new Move(
                    piece.getID(),
                    piece.getPlace(),
                    move,
                    false,
                    amIWhite
            );

            gameState.makeMove(piece, move);

            sendMoveToFirebase(m);

            myTurn = false;

            boardView.switchTurn();

            boardView.invalidate();
        });
    }

    public void applyOpponentMove(Move move){

        Piece piece =
                gameState.getBoard().findPiece(move.getBefore());

        gameState.makeMove(piece, move.getChange());

        myTurn = true;

        boardView.switchTurn();

        boardView.invalidate();
    }

    private void sendMoveToFirebase(Move move){

        gameRef.child("moves").push().setValue(move);

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
}
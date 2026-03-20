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
import com.example.new_chess.game.*;
import com.example.new_chess.game.Move;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;
import com.example.new_chess.pieces.Bishop;
import com.example.new_chess.pieces.Knight;
import com.example.new_chess.pieces.Pawn;
import com.example.new_chess.pieces.Piece;
import com.example.new_chess.pieces.Queen;
import com.example.new_chess.pieces.Rook;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jspecify.annotations.NonNull;

public class OnlineGameActivity extends AppCompatActivity {

    private ChessBoardView boardView;
    private GameState gameState;
    private DatabaseReference gameRef;
    private boolean gameReady = false;
    private String gameID;
    private boolean amIWhite;
    private boolean myTurn;
    private String promotionType = null;
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
        amIWhite = getIntent().getBooleanExtra("PLAYER_COLOR", false);

        if (gameID == null) {
            Log.e("GAME", "gameID is NULL!");
            finish();
            return;
        }

        gameRef = FirebaseDatabase
                .getInstance()
                .getReference("games")
                .child(gameID);


        boardView = findViewById(R.id.chessBoard);

        Player white = new Player(0);
        Player black = new Player(1);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //setupFirebaseListener();

        Board board = new Board(white, black);
        gameState = new GameState(new Board(board.getPlayer(0), board.getPlayer(1)));

        boardView.setBoard(board, gameState, !amIWhite); //am I black?

        myTurn = amIWhite;
        gameReady = true;


        listenForMoves();

        boardView.setMoveListener((piece, move) -> {

            if(!gameReady) return;
            if(!myTurn) return;

            gameState.setPawnPromotionListener(new GameState.PromotionListener() {
                @Override
                public void onPawnPromotion(Piece pawn, Point move) {
                    if(myTurn) {
                        showPromotionMenu(pawn, move);
                    }
                }
            });
            Move m = new Move(
                    piece.getID(),
                    piece.getPlace(),
                    move,
                    promotionType,
                    amIWhite
            );
            sendMoveToFirebase(m);

            gameState.makeMove(piece, move);


            int isCheckmate = gameState.checkMate();
            if(isCheckmate != -1){
                showWinDialog(isCheckmate);
            }


            myTurn = false;
            promotionType = null;

            boardView.switchTurn();
            boardView.invalidate();

        });

        Log.d("TURN", "amIWhite=" + amIWhite);
        Log.d("TURN", "myTurn=" + myTurn);


    }


    private void sendMoveToFirebase(Move move) {

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
                if (move.isWhiteMove() == amIWhite) return;

                Piece piece = null;
                if (amIWhite) {
                    piece = gameState.getBoard().getPlayer(0).getPieces()[move.getPieceID()];
                } else {
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

    private void listenForMoves() {

        DatabaseReference movesRef = FirebaseDatabase.getInstance()
                .getReference("games")
                .child(gameID)
                .child("moves");

        movesRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                Move move = snapshot.getValue(Move.class);

                if (move == null) return;

                // Ignore your own move
                if (move.isWhiteMove() == amIWhite) return;

                applyMoveFromFirebase(move);

                if(move.getPromotion() != null){
                    promoteEnemyPawn(move);
                }


            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void applyMoveFromFirebase(Move move) {

        Piece piece = gameState.getBoard().getPieceById(move.getPieceID(), move.isWhiteMove());

        if (piece == null) {
            Log.e("MOVE", "Piece not found!");
            return;
        }

        int isCheckmate = gameState.checkMate();
        if(isCheckmate != -1){
            showWinDialog(isCheckmate);
        }

        myTurn = true;

        if(isPromotion(move)){
            gameState.makeMove(piece, move.getChange(), false);
            myTurn = false;
            boardView.switchTurn();
        }
        else if (move.getPromotion() == null) {
            gameState.makeMove(piece, move.getChange());
        }

        boardView.switchTurn();
        boardView.invalidate();
    }

    private void showWinDialog(int loser) {

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

    private void showPromotionMenu(Piece pawn, Point move) { //in MainActivity because it needs an activity

        String[] options = {"Queen", "Rook", "Bishop", "Knight"};

        new AlertDialog.Builder(this)
                .setTitle("Promote Pawn")
                .setItems(options, (dialog, which) -> {

                    Piece newPiece = null;


                    switch(which){
                        case 0:
                            newPiece = new Queen(move, pawn.getColour(), pawn.getPlayer(), pawn.getID());
                            break;

                        case 1:
                            newPiece = new Rook(move, pawn.getColour(), pawn.getPlayer(), pawn.getID());
                            break;

                        case 2:
                            newPiece = new Bishop(move, pawn.getColour(), pawn.getPlayer(), pawn.getID());
                            break;

                        case 3:
                            newPiece = new Knight(move, pawn.getColour(), pawn.getPlayer(), pawn.getID());
                            break;
                    }

                    gameState.promotePawn(pawn, newPiece);

                    boardView.invalidate();
                    promotionType = gameState.getBoard().getBoard()[move.getX()][move.getY()].getClass().getSimpleName();

                    Move m = new Move(
                            pawn.getID(),
                            pawn.getPlace(),
                            move,
                            promotionType,
                            amIWhite
                    );
                    sendMoveToFirebase(m);

                })
                .show();
    }

    public void promoteEnemyPawn(Move move){
        String promotionClass = move.getPromotion();
        Piece newPiece = null;
        Piece pawn = gameState.getBoard().getBoard()[move.getChange().getX()][move.getChange().getY()];
        if(pawn instanceof Pawn){
            if(promotionClass.equals("Queen"))
                newPiece = new Queen(move.getChange(), pawn.getColour(), pawn.getPlayer(), pawn.getID());
            if(promotionClass.equals("Rook"))
                newPiece = new Rook(move.getChange(), pawn.getColour(), pawn.getPlayer(), pawn.getID());
            if(promotionClass.equals("Knight"))
                newPiece = new Knight(move.getChange(), pawn.getColour(), pawn.getPlayer(), pawn.getID());
            if(promotionClass.equals("Bishop"))
                newPiece = new Bishop(move.getChange(), pawn.getColour(), pawn.getPlayer(), pawn.getID());

            gameState.promotePawn(pawn, newPiece);
            myTurn = true;

            boardView.invalidate();
        }
    }

    public boolean isPromotion(Move move){
        Piece piece = gameState.getBoard().getBoard()[move.getBefore().getX()][move.getBefore().getY()];
        if (piece instanceof Pawn && (move.getChange().getY() == 0 || move.getChange().getY() == 7))
            return true;
        return false;
    }

}
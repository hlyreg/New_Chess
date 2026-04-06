package com.example.new_chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new_chess.firebase.ThemeManager;
import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.*;
import com.example.new_chess.pieces.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocalGameActivity extends AppCompatActivity {
    private GameState game;
    private ChessBoardView chessBoardView;
    private TextView bottomPlayerName;
    private TextView topPlayerName;
    private ImageButton btnUndo;
    private ImageButton btnRedo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_local_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ThemeManager.applyTheme(
                findViewById(android.R.id.content),
                ThemeManager.getTheme(this)
        );

        bottomPlayerName = findViewById(R.id.bottomPlayerName);
        topPlayerName = findViewById(R.id.topPlayerName);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        chessBoardView = findViewById(R.id.chessBoard);
        Player white = new Player(0);
        Player black = new Player(1);

        Board board = new Board(white, black);
        game = new GameState(new Board(board.getPlayer(0), board.getPlayer(1)));
        chessBoardView.setBoard(board, game);
        updateTurnUI();

        game.setPawnPromotionListener(new GameState.PromotionListener() {
            @Override
            public void onPawnPromotion(Piece pawn, Point move) {
                showPromotionMenu(pawn, move);
            }
        });

        chessBoardView.setMoveListener((piece, move) -> {

            game.makeMove(piece, move);

            int isCheckmate = game.checkMate(chessBoardView.isWhiteTurn());
            if (game.isThreefoldRepetition() || game.getMoveCounter(0) >= 50 || game.getMoveCounter(1) >= 50) {
                showWinDialog(-2);
            }
            if(isCheckmate != -1){
                showWinDialog(isCheckmate);
            }

            chessBoardView.switchTurn();   // change turn
            updateTurnUI();

            chessBoardView.invalidate();
        });
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

                    game.promotePawn(pawn, newPiece);

                    chessBoardView.invalidate();

                })
                .show();
    }

    private void showWinDialog(int loser){

        String message = (loser == 1) ? "White wins!" : "Black wins!";
        if(loser == -2)
            message = "Draw!";

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

    public void updateTurnUI() {
        if (chessBoardView.isWhiteTurn()) {
            highlightView(bottomPlayerName);
            unhighlightView(topPlayerName);
        } else {
            highlightView(topPlayerName);
            unhighlightView(bottomPlayerName);
        }
    }

    public void highlightView(TextView view) {
        view.setBackgroundColor(0x55DB1A1A); // soft yellow glow
    }

    public void unhighlightView(TextView view) {
        view.setBackgroundColor(0x00000000); // transparent
    }


    public void undo(View v){
        if (game.canUndo()) {
            game.undo();
            chessBoardView.switchTurn();
            chessBoardView.invalidate();
            updateTurnUI();
        }
    }

    public void redo(View v){
        if (game.canRedo()) {
            game.redo();
            chessBoardView.switchTurn();
            chessBoardView.invalidate();
            updateTurnUI();
        }
    }





}

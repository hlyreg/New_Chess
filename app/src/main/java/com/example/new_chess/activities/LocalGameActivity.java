package com.example.new_chess.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new_chess.ChessBoardView;
import com.example.new_chess.R;
import com.example.new_chess.firebase.ThemeManager;
import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.*;
import com.example.new_chess.pieces.*;

public class LocalGameActivity extends AppCompatActivity {
    private GameState game;
    private ChessBoardView chessBoardView;
    private TextView bottomPlayerName;
    private TextView topPlayerName;
    private ImageButton btnUndo;
    private ImageButton btnRedo;


    // ______________Timer________________________________
    private CountDownTimer whiteTimer;
    private CountDownTimer blackTimer;

    private long whiteTimeLeft;
    private long blackTimeLeft;

    private boolean timerEnabled;


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

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        timerEnabled = prefs.getBoolean("timer_enabled", false);
        int minutes = prefs.getInt("timer_minutes", 5);

        whiteTimeLeft = minutes * 60 * 1000;
        blackTimeLeft = minutes * 60 * 1000;

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

            if (timerEnabled) {
                if (chessBoardView.isWhiteTurn()) {
                    // white just moved → switch to black
                    stopWhiteTimer();
                    whiteTimeLeft += 3000; //adding increment (3 seconds)
                    startBlackTimer();
                } else {
                    // black just moved → switch to white
                    stopBlackTimer();
                    blackTimeLeft += 3000;
                    startWhiteTimer();
                }
                bottomPlayerName.setText(formatTime(whiteTimeLeft));
                topPlayerName.setText(formatTime(blackTimeLeft));
            }

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

        if (timerEnabled) {
            bottomPlayerName.setText(formatTime(whiteTimeLeft));
            topPlayerName.setText(formatTime(blackTimeLeft));

            startWhiteTimer(); // white starts
        }
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

    private void stopWhiteTimer() {
        if (whiteTimer != null) whiteTimer.cancel();
    }

    private void stopBlackTimer() {
        if (blackTimer != null) blackTimer.cancel();
    }

    private void startWhiteTimer() {
        whiteTimer = new CountDownTimer(whiteTimeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                whiteTimeLeft = millisUntilFinished;
                bottomPlayerName.setText(formatTime(whiteTimeLeft));
            }

            @Override
            public void onFinish() {
                showWinDialog(0); // white lost
            }
        }.start();
    }

    private void startBlackTimer() {
        blackTimer = new CountDownTimer(blackTimeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                blackTimeLeft = millisUntilFinished;
                topPlayerName.setText(formatTime(blackTimeLeft));
            }

            @Override
            public void onFinish() {
                showWinDialog(1); // black lost
            }
        }.start();
    }

    public String formatTime(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    public void quit(View v){
        startActivity(new Intent(this, HomeActivity.class));
    }



}

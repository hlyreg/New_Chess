package com.example.new_chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.*;
import com.example.new_chess.pieces.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocalGameActivity extends AppCompatActivity {
    private GameState game;
    private ChessBoardView chessBoardView;


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


           chessBoardView = findViewById(R.id.chessBoard);
        Player white = new Player(0);
        Player black = new Player(1);  // get rid of true

        Board board = new Board(white, black); // get rid of true
        game = new GameState(new Board(board.getPlayer(0), board.getPlayer(1)));
        chessBoardView.setBoard(board, game);  // get rid of true

        game.setPawnPromotionListener(new GameState.PromotionListener() {
            @Override
            public void onPawnPromotion(Piece pawn, Point move) {
                showPromotionMenu(pawn, move);
            }
        });

        chessBoardView.setMoveListener((piece, move) -> {

            game.makeMove(piece, move);

            int isCheckmate = game.checkMate();
            if(isCheckmate != -1){
                showWinDialog(isCheckmate);
            }

            chessBoardView.switchTurn();   // change turn

            chessBoardView.invalidate();
        });
    }

    public void play(View view){


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




}

package com.example.new_chess;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.new_chess.firebase.ChatAdapter;
import com.example.new_chess.firebase.ChatMessage;
import com.example.new_chess.firebase.ColorScheme;
import com.example.new_chess.firebase.GameResult;
import com.example.new_chess.firebase.User;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OnlineGameActivity extends AppCompatActivity {

    private ChessBoardView boardView;
    private GameState gameState;
    private DatabaseReference gameRef;
    private ColorScheme colorScheme;


    private boolean gameReady = false;
    private boolean gameEnded = false;
    private String gameID;
    private boolean amIWhite;
    private boolean myTurn;
    private String promotionType = null;


    private Button btnDraw;
    private Button btnGiveUp;

    //user elements
    private String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String opponentUID;
    private TextView topPlayerName;
    private String opponentUsername;
    private TextView bottomPlayerName;
    private String myUsername;

    //chat elements
    private DatabaseReference chatRef;
    private RecyclerView chatRecycler;
    private EditText messageInput;
    private Button sendBtn;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

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
        topPlayerName = findViewById(R.id.topPlayerName);
        bottomPlayerName = findViewById(R.id.bottomPlayerName);
        btnDraw = findViewById(R.id.btnDraw);
        btnGiveUp = findViewById(R.id.btnGiveUp);


        Player white = new Player(0);
        Player black = new Player(1);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //setupFirebaseListener();

        Board board = new Board(white, black);
        gameState = new GameState(new Board(board.getPlayer(0), board.getPlayer(1)));

        setupColorScheme();
        boardView.setBoard(board, gameState, !amIWhite); //am I black?

        myTurn = amIWhite;
        gameReady = true;

        setupUsername(0);
        setupUsername(1);

        //chat setup -----------------------------------------------------
        chatRef = FirebaseDatabase.getInstance()
                .getReference("games")
                .child(gameID)
                .child("chat");

        listenForMessages();

        chatRecycler = findViewById(R.id.chatRecycler);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);

        adapter = new ChatAdapter(messages, myUID);
        chatRecycler.setAdapter(adapter);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));

        sendBtn.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if(text.isEmpty()) return;

            new Thread(() -> {
                ChatMessage msg = new ChatMessage(myUID, text, System.currentTimeMillis());
                chatRef.push().setValue(msg);
            }).start();

            messageInput.setText("");
        });
        //-------------------------------------------------------------------
        updateTurnUI();

        listenForMoves();
        listenForDrawOffers();
        listenForGameResult();
        btnDraw.setOnClickListener(v -> offerDraw());
        btnGiveUp.setOnClickListener(v -> giveUp());

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


            int isCheckmate = gameState.checkMate(boardView.isWhiteTurn());
            if (gameState.isThreefoldRepetition() || gameState.getMoveCounter(0) >= 50 || gameState.getMoveCounter(1) >= 50) {
                showWinDialog(-2);
            }
            if(isCheckmate != -1){
                GameResult result = new GameResult("win", isCheckmate);
                gameRef.child("gameResult").setValue(result);
            }


            myTurn = false;
            promotionType = null;

            updateTurnUI();

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


        myTurn = true;

        if(isPromotion(move)){
            gameState.makeMove(piece, move.getChange(), false);
            myTurn = false;
            boardView.switchTurn();
        }
        else if (move.getPromotion() == null) {
            gameState.makeMove(piece, move.getChange());
        }

        int isCheckmate = gameState.checkMate(boardView.isWhiteTurn());
        if (gameState.isThreefoldRepetition() || gameState.getMoveCounter(0) >= 50 || gameState.getMoveCounter(1) >= 50) {
            showWinDialog(-2);
        }
        if(isCheckmate != -1){
            showWinDialog(isCheckmate);
        }

        boardView.switchTurn();
        updateTurnUI();
        boardView.invalidate();
    }

    private void showWinDialog(int loser) {

        updateStats(loser);

        String message;

        if (loser == 1) { //if black lost
            if(amIWhite) //and I'm white, I win
                message = myUsername + " wins!";
            else   //or I'm black, then I lose
                message = opponentUsername+" wins!";
        }
        else if(loser == 0){  //if white lost
            if(amIWhite)
                message = opponentUsername+" wins!";
            else
                message = myUsername + " wins!";
        }
        else{
            message = "Draw!";
        }

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

    private void updateStats(int loser) {

        boolean iAmWinner = false;
        boolean draw = false;

        if (loser == 1) { // black lost , then white wins
            iAmWinner = amIWhite;
        } else if(loser == 0) { // white lost, then black wins
            iAmWinner = !amIWhite;
        }
        else{
            draw = true;
        }


        if (iAmWinner && !draw) {
            incrementField(myUID, "wins");
            incrementField(opponentUID, "losses");
        }
        else if(!iAmWinner && !draw) {
            incrementField(myUID, "losses");
            incrementField(opponentUID, "wins");
        }
        else{
            incrementField(myUID, "draws");
            incrementField(opponentUID, "draws");
        }
    }

    private void incrementField(String uid, String field) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child(field);

        ref.setValue(ServerValue.increment(1));
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

            updateTurnUI();

            boardView.invalidate();
        }
    }

    public boolean isPromotion(Move move){
        Piece piece = gameState.getBoard().getBoard()[move.getBefore().getX()][move.getBefore().getY()];
        if (piece instanceof Pawn && (move.getChange().getY() == 0 || move.getChange().getY() == 7))
            return true;
        return false;
    }

    public void listenForMessages(){
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String prevChildKey) {
                ChatMessage msg = snapshot.getValue(ChatMessage.class);
                if(msg == null) return;

                messages.add(msg);

                runOnUiThread(() -> {
                    adapter.notifyItemInserted(messages.size() - 1);
                    chatRecycler.scrollToPosition(messages.size() - 1);
                });
            }

            @Override public void onChildChanged(DataSnapshot s, String p) {}
            @Override public void onChildRemoved(DataSnapshot s) {}
            @Override public void onChildMoved(DataSnapshot s, String p) {}
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    public void updateTurnUI() {
        if (myTurn) {
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

    public void setupUsername(int player){ // 0=me, 1=opponent
        if(player == 0){
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(myUID);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        bottomPlayerName.setText(user.username);
                        myUsername = user.username;
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }

        else{
            DatabaseReference playersRef = FirebaseDatabase.getInstance()
                    .getReference("games")
                    .child(gameID);

            playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String whiteUID = snapshot.child("white").getValue(String.class);
                    String blackUID = snapshot.child("black").getValue(String.class);

                    opponentUID = myUID.equals(whiteUID) ? blackUID : whiteUID;

                    loadOpponentName(opponentUID);
                }

                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }

    }

    private void loadOpponentName(String opponentUID) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(opponentUID);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    topPlayerName.setText(user.username);
                    opponentUsername = user.username;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void offerDraw() {
        gameRef.child("drawOffer").setValue(myUID);

        new AlertDialog.Builder(this)
                .setTitle("Draw Offered")
                .setMessage("Waiting for opponent to respond...")
                .setPositiveButton("OK", null)
                .show();
        btnDraw.setEnabled(false);
    }

    private void listenForDrawOffers() {
        gameRef.child("drawOffer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String offerUID = snapshot.getValue(String.class);

                if (offerUID == null) return;

                // If YOU sent it → ignore
                if (offerUID.equals(myUID)) return;

                // Opponent offered draw → show dialog
                showDrawDialog();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void showDrawDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Draw Offer")
                .setMessage(opponentUsername + " offers a draw.")
                .setPositiveButton("Accept", (dialog, which) -> {
                    gameRef.child("drawOffer").removeValue();
                    GameResult result = new GameResult("draw", -1);
                    gameRef.child("gameResult").setValue(result);

                    showWinDialog(-2);
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    gameRef.child("drawOffer").removeValue();
                })
                .setCancelable(false)
                .show();
    }


    private void giveUp() {
        new AlertDialog.Builder(this)
                .setTitle("Give Up")
                .setMessage("Are you sure you want to resign?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    GameResult result = new GameResult("resign", amIWhite ? 0 : 1);
                    gameRef.child("gameResult").setValue(result);
                    gameRef.child("resignedBy").setValue(myUID);

                })
                .setNegativeButton("No", null)
                .show();
    }


    private void listenForGameResult() {
        gameRef.child("gameResult").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                GameResult result = snapshot.getValue(GameResult.class);
                if (result == null) return;

                handleGameResult(result);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void handleGameResult(GameResult result) {
        if (gameEnded) return;
        gameEnded = true;

        switch (result.type) {

            case "draw":
                showWinDialog(-2);
                break;

            case "win":
                showWinDialog(result.loser);
                break;

            case "resign":
                handleResignResult(result.loser);
                break;
        }
    }

    private void handleResignResult(int loser) {

        updateStats(loser);

        boolean iLost = (amIWhite && loser == 0) || (!amIWhite && loser == 1);

        String message;

        if (iLost) {
            message = "You gave up. " + opponentUsername + " wins.";
        } else {
            message = opponentUsername + " gave up. You win!";
        }

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


    private void applyColorScheme() {
        boardView.setColorScheme(colorScheme);
        findViewById(R.id.main).setBackgroundColor(
                Color.parseColor(colorScheme.background)
        );
    }

    public void setupColorScheme(){
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(myUID);
        userRef.child("colorScheme").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                colorScheme = snapshot.getValue(ColorScheme.class);

                if (colorScheme == null) {
                    colorScheme = new ColorScheme().getDefault();
                }

                applyColorScheme();
            }

            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
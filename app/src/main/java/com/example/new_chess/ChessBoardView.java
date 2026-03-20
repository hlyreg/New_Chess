package com.example.new_chess;

import android.content.Context;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import com.example.new_chess.game.Board;
import com.example.new_chess.game.GameState;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;
import com.example.new_chess.pieces.Bishop;
import com.example.new_chess.pieces.King;
import com.example.new_chess.pieces.Knight;
import com.example.new_chess.pieces.Pawn;
import com.example.new_chess.pieces.Piece;
import com.example.new_chess.pieces.Queen;
import com.example.new_chess.pieces.Rook;


public class ChessBoardView extends View {
    private Paint paint = new Paint();
    private Paint checkGlowPaint = new Paint();
    private Paint highlightPaint = new Paint();  // the colour place holder
    private OnMoveListener moveListener;
    private float squareSize;
    private GameState game;
    private Map<Class<? extends Piece>, Bitmap[]> pieceBitmaps;
    private int selectedX = -1; // horizontal, -1 because nothing has been selected yet
    private int selectedY = -1; // vertical
    private Piece selectedPiece = null;   // what piece is currently selected
    private List<Point> legalMoves = new ArrayList<>();
    private boolean whiteToMove = true;  // is it whites turn?
    private boolean isUserBlack = false;




    public ChessBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadPieceBitmaps();
    }



    public void setBoard(Board board, GameState game) {
        this.game = game;
        invalidate();
    }

    public void setBoard(Board board, GameState game, boolean isBlack) {
        this.game = game;
        this.isUserBlack = isBlack;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        highlightPaint.setColor(Color.parseColor("#88FFD54F")); // yellow-ish


        squareSize = getWidth() / 8f;

        for (Point p : legalMoves) {
            float left = displayX(p.getX()) * squareSize;
            float top  = displayY(p.getY()) * squareSize;
            canvas.drawRect(
                    left,
                    top,
                    left + squareSize,
                    top + squareSize,
                    highlightPaint
            );
        }

        drawBoard(canvas);
        drawSelection(canvas);
        drawLegalMoves(canvas);
        drawCheck(canvas);
        drawPieces(canvas);

    }
//------------------------------------------------------DRAW FUNCTIONS--------------------------------------
    private void drawBoard(Canvas canvas) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {

                if ((x + y) % 2 == 0) {
                    paint.setColor(Color.parseColor("#94B4C1"));
                } else {
                    paint.setColor(Color.parseColor("#213448"));
                }

                canvas.drawRect(
                        displayX(x) * squareSize,
                        displayY(y) * squareSize,
                        (displayX(x) + 1) * squareSize,
                        (displayY(y) + 1) * squareSize,
                        paint
                );
            }
        }
    }
    private void drawPieces(Canvas canvas) {
        if (game.getBoard() == null) return;

        Piece[][] state = game.getBoard().getBoard();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {

                Piece place = state[x][y];
                if (place == null) continue;

                Bitmap[] pair = pieceBitmaps.get(place.getClass());
                if (pair == null) continue;

                Bitmap bitmap = pair[place.getColour()];

                float left = displayX(x) * squareSize;
                float top  = displayY(y) * squareSize;

                Bitmap scaled = Bitmap.createScaledBitmap(
                        bitmap,
                        (int) squareSize,
                        (int) squareSize,
                        true
                );

                canvas.drawBitmap(scaled, left, top, null);
            }
        }
    }


    private void drawSelection(Canvas canvas) {
        if (selectedX == -1 || selectedY == -1) return;
        highlightPaint.setColor(Color.parseColor("#90926F54"));

        canvas.drawRect(
                displayX(selectedX) * squareSize,
                displayY(selectedY) * squareSize,
                (displayX(selectedX)+1) * squareSize,
                (displayY(selectedY) + 1) * squareSize,
                highlightPaint
        );
    }

    private void drawLegalMoves(Canvas canvas) {
        highlightPaint.setColor(Color.parseColor("#90547792"));

        for (Point p : legalMoves) {

            float left = displayX(p.getX()) * squareSize;
            float top  = displayY(p.getY()) * squareSize;

            canvas.drawCircle(
                    left + squareSize / 2,
                    top + squareSize / 2,
                    squareSize / 4,
                    highlightPaint
            );


            // Optional: show capture differently
            if (p.isTakes()) {
                canvas.drawCircle(
                        left + squareSize / 2,
                        top + squareSize / 2,
                        squareSize / 4,
                        highlightPaint
                );
            }
        }
    }
    private void drawCheck(Canvas canvas) {

        Point kingSquare = game.getKingInCheck();
        if (kingSquare == null) return;

        float centerX = kingSquare.getX() * squareSize + squareSize / 2f;
        float centerY = kingSquare.getY() * squareSize + squareSize / 2f;

        float radius = squareSize * 0.8f;

        RadialGradient gradient = new RadialGradient(
                centerX,
                centerY,
                radius,
                new int[]{
                        Color.argb(180, 255, 0, 0),  // strong red center
                        Color.argb(80, 255, 0, 0),   // mid fade
                        Color.argb(0, 255, 0, 0)     // transparent edge
                },
                new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP
        );

        checkGlowPaint.setShader(gradient);

        canvas.drawCircle(centerX, centerY, radius, checkGlowPaint);

        checkGlowPaint.setShader(null); // reset paint
    }


//------------------------------------------------------------------

    private void loadPieceBitmaps() {
        pieceBitmaps = new HashMap<>();

        pieceBitmaps.put(Pawn.class, new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.white_pawn),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_pawn)
        });

        pieceBitmaps.put(Rook.class, new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.white_rook),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_rook)
        });

        pieceBitmaps.put(Knight.class, new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.white_knight),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_knight)
        });

        pieceBitmaps.put(Bishop.class, new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.white_bishop),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_bishop)
        });

        pieceBitmaps.put(Queen.class, new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.white_queen),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_queen)
        });

        pieceBitmaps.put(King.class, new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.drawable.white_king),
                BitmapFactory.decodeResource(getResources(), R.drawable.black_king)
        });
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return false; // only handle taps
        }

        int x = (int) (event.getX() / squareSize); // column
        int y = (int) (event.getY() / squareSize); // row


        if (x < 0 || x > 7 || y < 0 || y > 7) return false;

        Point tapped = screenToBoard(event.getX(), event.getY());
        selectedX = tapped.getX();
        selectedY = tapped.getY();

        handleTap(tapped);  // handle selection
        invalidate();      // redraw board
        return true;
    }
    private void handleTap(Point tap) {
        Log.d("TAP", "Tap x=" + tap.getX() + " y=" + tap.getY());

        Piece tappedPiece = game.getBoard().findPiece(tap);

        // No piece selected yet → select one
        if (selectedPiece == null) {
            if (tappedPiece != null && tappedPiece.getColour() == (whiteToMove ? 0 : 1)) {
                selectedPiece = tappedPiece;
                legalMoves = selectedPiece.getLegalMoves(game.getBoard(), game.getBoard().wasLastMovePawnTwo());
            }
            return;
        }

        // Piece already selected → try to move
        for (Point move : legalMoves) {
            if (move.compare(tap.getX(), tap.getY())) {
                if(moveListener != null){
                    moveListener.onMove(selectedPiece, move);
                }
                break;
            }
        }

        // Clear selection either way
        selectedPiece = null;
        selectedX = -1;
        selectedY = -1;
        legalMoves.clear();
        invalidate();
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        squareSize = Math.min(w, h) / 8;
    }

    private Point screenToBoard(float sx, float sy) {
        int x = (int) (sx / squareSize);
        int y = (int) (sy / squareSize);

        if (isUserBlack) {
            x = 7 - x;
            y = 7 - y;
        }

        return new Point(x, y);
    }
    public void switchTurn(){
        whiteToMove = !whiteToMove;
    }

    //______________________________________________________________________


    public interface OnMoveListener {
        void onMove(Piece piece, Point move);
    }

    public void setMoveListener(OnMoveListener listener){
        this.moveListener = listener;
    }

    public boolean isWhiteTurn(){
        return whiteToMove;
    }
    private int displayX(int x) {
        return isUserBlack ? 7 - x : x;
    }

    private int displayY(int y) {
        return isUserBlack ? 7 - y : y;
    }





}

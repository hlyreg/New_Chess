package com.example.new_chess;

import android.content.Context;
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
    private float squareSize;
    private GameState game;
    private Map<Class<? extends Piece>, Bitmap[]> pieceBitmaps;
    private int selectedX = -1; // horizontal, -1 because nothing has been selected yet
    private int selectedY = -1; // vertical
    private Paint highlightPaint = new Paint();  // the colour place holder
    private List<Point> legalMoves = new ArrayList<>();
    private Piece selectedPiece = null;   // what piece is currently selected
    private boolean whiteToMove = true;  // is it whites turn?




    public ChessBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadPieceBitmaps();
    }


    public void setBoard(Board board) {
        game = new GameState(new Board(board.getPlayer(0), board.getPlayer(1)));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint highlightPaint = new Paint();
        highlightPaint.setColor(Color.parseColor("#88FFD54F")); // yellow-ish

        squareSize = getWidth() / 8f;

        for (Point p : legalMoves) {
            float left = p.getX() * squareSize;
            float top =  p.getY() * squareSize;
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
        drawPieces(canvas);

    }

    private void drawBoard(Canvas canvas) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {

                if ((x + y) % 2 == 0) {
                    paint.setColor(Color.parseColor("#94B4C1"));
                } else {
                    paint.setColor(Color.parseColor("#213448"));
                }

                canvas.drawRect(
                        x * squareSize,
                        y * squareSize,
                        (x + 1) * squareSize,
                        (y + 1) * squareSize,
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

                float left = x * squareSize;
                float top = y * squareSize;

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
                game.makeMove(selectedPiece, move);
                whiteToMove = !whiteToMove;   // switch the whose turn it is
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





    private void drawSelection(Canvas canvas) {
        if (selectedX == -1 || selectedY == -1) return;
        highlightPaint.setColor(Color.parseColor("#90926F54"));

        canvas.drawRect(
                selectedX * squareSize,
                selectedY * squareSize,
                (selectedX + 1) * squareSize,
                (selectedY + 1) * squareSize,
                highlightPaint
        );
    }

    private void drawLegalMoves(Canvas canvas) {
        highlightPaint.setColor(Color.parseColor("#90547792"));

        for (Point p : legalMoves) {

            float left = p.getX() * squareSize;
            float top  = p.getY() * squareSize;

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        squareSize = Math.min(w, h) / 8;
    }

    private Point screenToBoard(float sx, float sy) {
        int x = (int) (sx / squareSize);
        int y = (int) (sy / squareSize);
        return new Point(x, y);
    }






}

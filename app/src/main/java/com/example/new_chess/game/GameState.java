package com.example.new_chess.game;

import com.example.new_chess.LocalGameActivity;
import com.example.new_chess.pieces.Pawn;
import com.example.new_chess.pieces.Piece;
import com.example.new_chess.pieces.Queen;

import java.util.Stack;

public class GameState {
    private final Stack<Board> pastBoards = new Stack<>();
    private final Stack<Board> futureBoards = new Stack<>();
    private PromotionListener promotionListener;
    private Board currentBoard;

    public GameState(Board start) {
        currentBoard = start;
    }

    public Board getBoard() {
        return currentBoard;
    }

    public void makeMove(Piece piece, Point move) {
        pastBoards.push(currentBoard);

        // create new board with piece copies & updated positions
        currentBoard = new Board(currentBoard, piece.getPlace(), move, piece);
        futureBoards.clear();

        // Check pawn promotion
        if (piece instanceof Pawn && (move.getY() == 0 || move.getY() == 7)) {
            if (promotionListener != null) {
                promotionListener.onPawnPromotion(piece, move);
            }

        }
    }



    public void promotePawn(Piece pawn, Piece newPiece){

        Board board = currentBoard;

        Point pawnP = pawn.getPlace();
        Point newP = newPiece.getPlace();

        board.getBoard()[pawnP.getX()][pawnP.getY()] = null;
        board.getBoard()[newP.getX()][newP.getY()] = newPiece;


    }


    public boolean canUndo() {
        return !pastBoards.isEmpty();
    }

    public boolean canRedo() {
        return !futureBoards.isEmpty();
    }

    public void undo() {
        if (!canUndo()) return;
        futureBoards.push(currentBoard);
        currentBoard = pastBoards.pop();
    }

    public void redo() {
        if (!canRedo()) return;
        pastBoards.push(currentBoard);
        currentBoard = futureBoards.pop();
    }

    public void setPawnPromotionListener(PromotionListener listener) {
        this.promotionListener = listener;
    }
    public interface PromotionListener {
        void onPawnPromotion(Piece pawn, Point move);
    }


}

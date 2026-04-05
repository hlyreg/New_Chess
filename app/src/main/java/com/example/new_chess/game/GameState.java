package com.example.new_chess.game;

import com.example.new_chess.pieces.Pawn;
import com.example.new_chess.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameState {
    private final Stack<Board> pastBoards = new Stack<>();
    private final Stack<Board> futureBoards = new Stack<>();
    private PromotionListener promotionListener;
    private Board currentBoard;
    private int moveCounterWhite = 0;
    private int moveCounterBlack = 0;

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

        if (piece instanceof Pawn || move.isTakes()) {
            if(piece.getColour() == 0)
                moveCounterWhite = 0;
            moveCounterBlack = 0;
        } else {
            if(piece.getColour() == 0)
                moveCounterWhite++;
            moveCounterBlack++;
        }

        // Check pawn promotion
        if (piece instanceof Pawn && (move.getY() == 0 || move.getY() == 7)) {
            if (promotionListener != null) {
                promotionListener.onPawnPromotion(piece, move);
            }

        }
    }

    public void makeMove(Piece piece, Point move, boolean toPromote) {
        pastBoards.push(currentBoard);

        // create new board with piece copies & updated positions
        currentBoard = new Board(currentBoard, piece.getPlace(), move, piece);
        futureBoards.clear();

        // Check pawn promotion
        if(toPromote){
            if (piece instanceof Pawn && (move.getY() == 0 || move.getY() == 7)) {
                if (promotionListener != null) {
                    promotionListener.onPawnPromotion(piece, move);
                }

            }
        }
    }



    public void promotePawn(Piece pawn, Piece newPiece){

        Board board = currentBoard;

        Point pawnP = pawn.getPlace();
        Point newP = newPiece.getPlace();

        board.getBoard()[pawnP.getX()][pawnP.getY()] = null;
        board.getBoard()[newP.getX()][newP.getY()] = newPiece;
        board.getPlayer(newPiece.getColour()).getPieces()[newPiece.getID()] = newPiece;


    }

    public int checkMate(boolean isWhite){  // returns the colour of the player that lost
        Point checkPlace = getKingInCheck();
        Board board = this.getBoard();
        int turn = (isWhite)? 0:1;


        // if the king is in check, and none of the pieces can move, then the checked player loses.
        if(checkPlace != null){
            int color = board.getBoard()[checkPlace.getX()][checkPlace.getY()].getColour();
            boolean moving = canMove(color);
            return color;
        }
        if(!canMove(turn))
            return -2;  // if it's a draw, return -2
        return -1; // if no one lost return -1
    }


    public boolean canMove(int color){
        Board board = this.getBoard();
        Piece[] movingPieces = board.getPlayer(color).getPieces();
        List<Point> moves = new ArrayList<>();

        for(Piece piece : movingPieces){  //check if each piece can't move
            if(piece != null) {
                moves.addAll(piece.getLegalMoves(board, board.wasLastMovePawnTwo()));
            }
        }
        if(moves.isEmpty()) {
            return false;
        }
        return true;
    }

    public Point getKingInCheck() {

        Player white = currentBoard.getPlayer(0);
        Player black = currentBoard.getPlayer(1);

        Piece whiteKing = white.getPieces()[12];
        Piece blackKing = black.getPieces()[12];

        if(currentBoard.isSquareAttacked(
                whiteKing.getPlace().getY(),
                whiteKing.getPlace().getX(),
                black)){
            return whiteKing.getPlace();
        }


        if(currentBoard.isSquareAttacked(
                blackKing.getPlace().getY(),
                blackKing.getPlace().getX(),
                white)){
            return blackKing.getPlace();
        }

        return null;
    }

    public boolean isThreefoldRepetition() {
        int count = 0;

        for (Board b : pastBoards) {
            if (b.equals(currentBoard)) {
                count++;
            }
        }

        return count >= 2; // current position = 3rd occurrence
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

    public int getMoveCounter(int colour){
        if(colour == 0)
            return moveCounterWhite;
        return moveCounterBlack;
    }


}

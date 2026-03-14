package com.example.new_chess.game;

import com.example.new_chess.pieces.Pawn;
import com.example.new_chess.pieces.Piece;
import com.example.new_chess.pieces.Queen;

import java.util.Stack;

public class GameState {
    private final Stack<Board> pastBoards = new Stack<>();
    private final Stack<Board> futureBoards = new Stack<>();

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
        Piece movedPiece = currentBoard.getBoard()[move.getX()][move.getY()];
        if (movedPiece instanceof Pawn) {
            if (movedPiece.getColour() == 0 && move.getY() == 0) {
                promotePawn(move);
            }

            if (movedPiece.getColour() == 1 && move.getY() == 7) {
                promotePawn(move);
            }
        }
    }

    private void promotePawn(Point pos) {

        Piece pawn = currentBoard.getBoard()[pos.getX()][pos.getY()];
        Player owner = pawn.getPlayer();

        //for now we make the pawn automatically upgrade to a queen
        Queen queen = new Queen(pos, pawn.getColour(), owner, pawn.getID());

        currentBoard.getBoard()[pos.getX()][pos.getY()] = queen;

        owner.getPieces()[pawn.getID()] = queen;
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
}

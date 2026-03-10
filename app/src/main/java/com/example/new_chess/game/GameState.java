package com.example.new_chess.game;

import com.example.new_chess.pieces.Piece;

import java.util.Stack;

public class GameState {
    private final Stack<Board> past = new Stack<>();
    private final Stack<Board> future = new Stack<>();

    private Board current;

    public GameState(Board start) {
        current = start;
    }

    public Board getBoard() {
        return current;
    }

    public void makeMove(Piece piece, Point move) {
        past.push(current);
        current = new Board(current, piece.getPlace(), move, piece);
        future.clear();
    }

    public boolean canUndo() {
        return !past.isEmpty();
    }

    public boolean canRedo() {
        return !future.isEmpty();
    }

    public void undo() {
        if (!canUndo()) return;
        future.push(current);
        current = past.pop();
    }

    public void redo() {
        if (!canRedo()) return;
        past.push(current);
        current = future.pop();
    }
}

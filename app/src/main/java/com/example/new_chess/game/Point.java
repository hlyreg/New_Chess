package com.example.new_chess.game;

public class Point {
    private int x;
    private int y;
    private boolean takes;
    private boolean pawn_twice;
    private boolean castling;
    public Point(int y, int x){
        this.x = x;
        this.y = y;
        takes = false;      // does the move kill a piece
        pawn_twice = false;     // does a pawn move twice, so that un poissant is possible
        castling = false;

    }
    public Point(int y, int x, boolean takes){
        this.x = x;
        this.y = y;
        this.takes = takes;
        castling = false;
    }

    public Point(int y, int x, boolean takes, boolean twice){
        this.x = x;
        this.y = y;
        this.takes = takes;
        this.pawn_twice = twice;
        castling = false;
    }

    public Point(int y, int x, boolean takes, boolean twice, boolean castling){
        this.x = x;
        this.y = y;
        this.takes = takes;
        this.pawn_twice = twice;
        this.castling = castling;
    }

    public boolean compare(int y, int x){
        if(this.y == y && this.x == x){
            return true;
        }
        return false;
    }



    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isTakes() {
        return takes;
    }

    public boolean isPawn_twice() {
        return pawn_twice;
    }
}

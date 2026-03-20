package com.example.new_chess.game;

public class Point {
    private int x;
    private int y;
    private boolean takes;  // is it a move that kills a piece?
    private boolean pawn_twice;  // is it a move where a pawn moves twice?
    private boolean enPassant;
    private boolean castling;   // is it a move where castling is triggered?

    public Point(){}

    public Point (Point point){
        this.x = point.getX();
        this.y = point.getY();
        takes = point.isTakes();
        pawn_twice = point.isPawn_twice();
        enPassant = point.isEnPassant();
        castling = point.isCastling();
    }

    public Point(int x, int y){
        this.x = x;
        this.y = y;
        takes = false;      // does the move kill a piece
        pawn_twice = false;     // does a pawn move twice, so that un poissant is possible
        castling = false;
        enPassant = false;

    }
    public Point(int x, int y, boolean takes){
        this.x = x;
        this.y = y;
        this.takes = takes;
        castling = false;
        enPassant = false;
    }

    public Point(int x, int y, boolean takes, boolean twice){
        this.x = x;
        this.y = y;
        this.takes = takes;
        this.pawn_twice = twice;
        castling = false;
        enPassant = false;
    }

    public Point(int x, int y, boolean takes, boolean twice, boolean castling){
        this.x = x;
        this.y = y;
        this.takes = takes;
        this.pawn_twice = twice;
        this.castling = castling;
        enPassant = false;
    }

    public Point(int x, int y, boolean takes, boolean twice, boolean castling, boolean enPassant){
        this.x = x;
        this.y = y;
        this.takes = takes;
        this.pawn_twice = twice;
        this.castling = castling;
        this.enPassant = enPassant;
    }

    public boolean compare(int x, int y){
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
    public boolean isEnPassant(){return enPassant;}
    public boolean isCastling() {
        return castling;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point)) return false;
        Point p = (Point) obj;
        return this.x == p.x && this.y == p.y;
    }

    @Override
    public int hashCode() {
        return 31 * y + x;
    }
}

package com.example.new_chess.game;

public class Move {
    private int pieceID;
    private Point change;
    private Point before;
    private boolean promotion;
    private boolean AmIWhite;

    public Move(){}
    public Move(int pieceID, Point before,  Point change, boolean promotion, boolean AmIWhite){
        this.pieceID = pieceID;
        this.change = change;
        this.before = before;
        this.promotion = promotion;
        this.AmIWhite = AmIWhite;
    }

    public int getPieceID(){
        return pieceID;
    }

    public boolean isAmIWhite() {
        return AmIWhite;
    }

    public Point getChange(){
        return change;
    }

    public Point getBefore(){
        return before;
    }

    public boolean getPromotion(){
        return promotion;
    }

}

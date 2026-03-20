package com.example.new_chess.game;

public class Move {
    private int pieceID;
    private Point change;
    private Point before;
    private String promotion;
    private boolean whiteMove;

    public Move(){}
    public Move(int pieceID, Point before,  Point change, String promotion, boolean AmIWhite){
        this.pieceID = pieceID;
        this.change = change;
        this.before = before;
        this.promotion = promotion;
        this.whiteMove = AmIWhite;
    }

    public int getPieceID(){
        return pieceID;
    }

    public boolean isWhiteMove() {
        return whiteMove;
    }

    public Point getChange(){
        return change;
    }

    public Point getBefore(){
        return before;
    }

    public String getPromotion(){
        return promotion;
    }

}

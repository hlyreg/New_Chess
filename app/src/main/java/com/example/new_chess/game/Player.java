package com.example.new_chess.game;

import com.example.new_chess.pieces.Bishop;
import com.example.new_chess.pieces.King;
import com.example.new_chess.pieces.Knight;
import com.example.new_chess.pieces.Pawn;
import com.example.new_chess.pieces.Piece;
import com.example.new_chess.pieces.Queen;
import com.example.new_chess.pieces.Rook;

public class Player {

    private int colour; // what colour player, 0 = white, 1 = black
    private Piece[] pieces = new Piece[16];

    public Player(int colour){
        this.colour = colour;
        setPieces();


    }

    private void setPieces(){
        if(this.colour == 0){
            for(int x = 0; x < 8; x++){
                pieces[x] = new Pawn(new Point(x, 6), colour, this, x);

                if(x % 7 == 0)
                    pieces[x+8] = new Rook(new Point(x, 7), colour, this, x+8);

                else if(x == 1 || x == 6)
                    pieces[x+8] = new Knight(new Point(x, 7), colour, this, x+8);

                else if(x == 2 || x == 5)
                    pieces[x+8] = new Bishop(new Point(x, 7), colour, this, x+8);
            }
            pieces[11] = new Queen(new Point(3, 7), colour, this, 14);
            pieces[12] = new King(new Point(4, 7), colour, this, 15);
        }

        else{
            for(int x = 0; x < 8; x++){
                pieces[x] = new Pawn(new Point(x, 1), colour, this, x);

                if(x % 7 == 0)
                    pieces[x+8] = new Rook(new Point(x, 0), colour, this, x+8);

                else if(x == 1 || x == 6)
                    pieces[x+8] = new Knight(new Point(x, 0), colour, this, x+8);

                else if(x == 2 || x == 5)
                    pieces[x+8] = new Bishop(new Point(x, 0), colour, this, x+8);
            }
            pieces[11] = new Queen(new Point(3, 0), colour, this, 14);
            pieces[12] = new King(new Point(4, 0), colour, this, 15);
        }
    }

    public Piece[] getPieces(){
        return this.pieces;
    }

    public Piece getPiece(Point point){
        int x = point.getX();
        int y = point.getY();
        if(x<8 && x>0 && y<8 && y>0) {
            for (Piece p : pieces) {
                if (p.isAlive() && p.getPlace().compare(x, y))
                    return p;
            }
        }
        return null;
    }

}


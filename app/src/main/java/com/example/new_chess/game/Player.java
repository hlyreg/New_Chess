package com.example.new_chess.game;

import com.example.new_chess.pieces.Bishop;
import com.example.new_chess.pieces.King;
import com.example.new_chess.pieces.Knight;
import com.example.new_chess.pieces.Pawn;
import com.example.new_chess.pieces.Piece;
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
                pieces[x] = new Pawn(new Point(1, x), colour, this, x);

                if(x % 7 == 0)
                    pieces[x+8] = new Rook(new Point(0, x), colour, this, x+8);

                else if(x == 1 || x == 6)
                    pieces[x+8] = new Knight(new Point(0, x), colour, this, x+8);

                else if(x == 2 || x == 5)
                    pieces[x+8] = new Bishop(new Point(0, x), colour, this, x+8);    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!add QUEEN
            }
            pieces[15] = new King(new Point(0, 4), colour, this, 15);
        }

        else{
            for(int x = 0; x < 8; x++){
                pieces[x] = new Pawn(new Point(6, x), colour, this, x);

                if(x % 7 == 0)
                    pieces[x+8] = new Rook(new Point(7, x), colour, this, x+8);

                else if(x == 1 || x == 6)
                    pieces[x+8] = new Knight(new Point(7, x), colour, this, x+8);

                else if(x == 2 || x == 5)
                    pieces[x+8] = new Bishop(new Point(7, x), colour, this, x+8);    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!add QUEEN
            }
            pieces[15] = new King(new Point(7, 4), colour, this, 15);
        }
    }

    public Piece[] getPieces(){
        return this.pieces;
    }

}


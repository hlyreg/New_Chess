package com.example.new_chess.pieces;

import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece{
    public boolean moved;  //for castling

    public Rook(Point place, int colour, Player player, int ID){
        super(place, colour, player, ID);
        this.moved = false;
    }

    @Override
    public List<Point> getMoves(Board current_board, boolean trigger, Player opponent) {

        List<Point> moves = new ArrayList<>();

//_________________________________________________________________________

        moves.addAll(this.continueDirection(0,1, current_board));     //UP

        moves.addAll(this.continueDirection(0,-1, current_board));    //DOWN

        moves.addAll(this.continueDirection(-1,0, current_board));    //LEFT

        moves.addAll(this.continueDirection(1,0, current_board));     //RIGHT
//_______________________________________________________________________________


        return moves;
    }

    @Override
    public void move(Point newPlace) {
        this.place = newPlace;
        this.moved = true;
    }

    public boolean getMoved(){
        return this.moved;
    }

    public Point castling(){
        if(player.getPieces()[11] instanceof King) {
            King myking = (King) player.getPieces()[11];
            if(!myking.getMoved() && !this.moved) {            //if the king hasn't moved yet
                if (this.place.compare(this.place.getY(), 7)) {
                    this.place = new Point(this.place.getY(), 5);
                    return this.place;
                } else {
                    this.place = new Point(this.place.getY(), 2);
                    return this.place;
                }
            }
        }
        return null;
    }

}

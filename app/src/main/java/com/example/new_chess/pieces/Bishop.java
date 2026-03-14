package com.example.new_chess.pieces;
import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece{

    public Bishop(Point place, int colour, Player player, int ID){
        super(place, colour, player, ID);
    }

    public Bishop(Bishop other, Player player){
        super(other, player);
    }


    @Override
    public List<Point> getMoves(Board current_board, boolean trigger, Player opponent) {
        List<Point> moves = new ArrayList<>();

//________________________________________________________________________

        moves.addAll(this.continueDirection(1,1, current_board));  //top right

        moves.addAll(this.continueDirection(-1,1, current_board));   //top left

        moves.addAll(this.continueDirection(-1,-1, current_board));   //bottom left

        moves.addAll(this.continueDirection(1,-1, current_board));    //bottom right

//___________________________________________________________________________

        return moves;
    }

    @Override
    public Piece copy(Player player) {
        return new Bishop(this, player);
    }



    @Override
    public void move(Point newPlace) {
        this.place = newPlace;
    }
}

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

    @Override
    public List<Point> getMoves(Board current_board, boolean trigger, Player opponent) {
        int x = this.place.getX();
        int y = this.place.getY();
        String[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();

//________________________________________________________________________

        moves.addAll(this.continueDirection(1,1, current_board, opponent));  //top right

        moves.addAll(this.continueDirection(-1,1, current_board, opponent));   //top left

        moves.addAll(this.continueDirection(-1,-1, current_board, opponent));   //bottom left

        moves.addAll(this.continueDirection(1,-1, current_board, opponent));    //bottom right

//___________________________________________________________________________

        return moves;
    }


    @Override
    public void move(Point newPlace) {
        this.place = newPlace;
    }
}

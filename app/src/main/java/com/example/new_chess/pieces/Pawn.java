package com.example.new_chess.pieces;

import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    private int moved;  // if moved = 0, it hasn't moved yet, if it's 2, it moved 2, if it's 1, that means it's moved once since it was 2 or 0.


    public Pawn(Point place, int colour, Player player, int ID){
        super(place, colour, player, ID);
        this.moved = 0;
    }

    @Override
    public List<Point> getMoves(Board current_board, boolean is_twice, Player opponent) {
        int x = this.place.getX();
        int y = this.place.getY();
        Piece[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();
//_______________________________White_______________________________________________
        if(y+1 < 8) {

            if (colour == 1) {
                // forward
                if (isEmpty(board, x, y + 1)) {
                    moves.add(new Point(x, y + 1));
                }

                // double
                if (moved == 0 && y+2 < 8 && isEmpty(board, x, y + 1) && isEmpty(board,x, y + 2)) {
                    moves.add(new Point(x, y + 2, false, true));
                }

                // captures
                if (x-1 >= 0 && !isEmpty(board, x - 1, y + 1) && !isComrade(board, x - 1, y + 1)) {
                    moves.add(new Point(x - 1, y + 1, true));
                }
                if (x+1 <= 7 && !isEmpty(board, x + 1, y + 1) && !isComrade(board, x + 1, y + 1)) {
                    moves.add(new Point(x + 1, y + 1, true));
                }
            }
        }

//_________________________________Black________________________________________

        if(y-1>= 0) {

            if (colour == 0) {  // changed colour

                // forward one square
                if ( isEmpty(board, x, y - 1)) {
                    moves.add(new Point(x, y - 1));
                }

                // forward two squares (first move only)
                if (moved == 0 && y - 2 >= 0 && isEmpty(board, x, y - 1) && isEmpty(board, x, y - 2)) {
                    moves.add(new Point(x, y - 2, false, true));
                }

                // capture diagonally left
                if (x - 1 >= 0 && !isEmpty(board, x - 1, y - 1) &&!isComrade(board, x - 1, y - 1)) {
                    moves.add(new Point(x - 1, y - 1, true));
                }

                // capture diagonally right
                if (x + 1 <= 7 && !isEmpty(board, x + 1, y - 1) && !isComrade(board, x + 1, y - 1)) {
                    moves.add(new Point(x + 1, y - 1, true));
                }
            }


        }


        return moves;
    }

    public void move(Point new_place){ // the function that changes the location of the pawn
        this.place = new_place;

        if(this.moved == 0)     //did it move for the first time
            this.moved = 1;
        if(new_place.isPawn_twice())    // did it move twice
            this.moved = 2;
    }

    public void cancel_twice(){     // turns off the lamp that signals if un poissant is possible
        this.moved = 1;
    }

    public int getMoved(){
        return this.moved;
    }
}

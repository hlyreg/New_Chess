package com.example.new_chess.pieces;

import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    private boolean isUserBlack = false;
    private int moved;  // if moved = 0, it hasn't moved yet, if it's 2, it moved 2, if it's 1, that means it's moved once since it was 2 or 0.


    public Pawn(Point place, int colour, Player player, int ID){
        super(place, colour, player, ID);
        this.moved = 0;
    }
    public Pawn(Point place, int colour, Player player, int ID, boolean isBlack){
        super(place, colour, player, ID);
        this.moved = 0;
        this.isUserBlack = isBlack;
    }
    public Pawn(Pawn other, Player player){
        super(other, player);
        this.moved = other.moved;
    }

    @Override
    public List<Point> getMoves(Board current_board, boolean is_twice, Player opponent) {
        int x = this.place.getX();
        int y = this.place.getY();
        Piece[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();
//_______________________________White_______________________________________________
        if(y+1 < 8) {

            if (colour == 1 || (isUserBlack && colour == 0)) {
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

            if (colour == 0 || (isUserBlack && colour == 1)) {  // changed colour

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

        // is en passant possible:
        if (current_board.wasLastMovePawnTwo()) {

            Point last = current_board.getLastMovePawnTwo();  // last = the position of the pawn that moved 2

            if (last.getY() == place.getY()) { // are they on the same y

                if (Math.abs(last.getX() - place.getX()) == 1) {   // is the pawn right next to us?

                    int direction = (colour == 0) ? -1 : 1;  // if white go up(-1) if black go down(1)

                    if(isUserBlack)
                        direction = (colour == 1) ? -1 : 1;  //flip the direction if it's from black pov

                    moves.add(new Point(

                            last.getX(),
                            place.getY() + direction,
                            true,     // capture
                            false,    // en passant
                            false,
                            true// en passant
                        ));

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
    @Override
    public Piece copy(Player player) {
        return new Pawn(this, player);
    }
    public void cancel_twice(){     // turns off the lamp that signals if un poissant is possible
        this.moved = 1;
    }

    public int getMoved(){
        return this.moved;
    }
}

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
        String[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();
//_______________________________White_______________________________________________
        if(y++ < 8) {

            if(moved == 0 && this.colour == 0 && isEmpty(board, 2, x) &&
                    !isCheck(opponent, this.ID, new Point(y++, x), current_board)){  // if this is the pawn's first move and the player is white, check if it can move forward once and twice.
                moves.add(new Point(y++, x));
                if(isEmpty(board, y+2, x)&& !isCheck(opponent, this.ID, new Point(y+2, x), current_board))
                    moves.add(new Point(y+2, x, false, true));
            }

            if (this.colour == 0 && isEmpty(board, y++, x) &&
                    !isCheck(opponent, this.ID, new Point(y++, x), current_board))  // check if it can move forward once as white.
                moves.add(new Point(y++, x));

            if (this.colour == 0 && x++ < 8 && !isEmpty(board, y++, x++) && !isComrade(board, y++, x++) &&
                    !isCheck(opponent, this.ID, new Point(y++, x++, true), current_board))        // check if white can take a piece
                moves.add(new Point(y++, x++, true));


            if (this.colour == 0 && x-- >= 0 && !isEmpty(board, y++, x--)  &&  !isComrade(board, y++, x--) &&
                    !isCheck(opponent, this.ID, new Point(y++, x--, true), current_board))
                moves.add(new Point(y++, x--, true));



            if(this.colour == 0 && board[4][x++].equals("Pawn-1")&& place.getY() == 4 && x++ < 8 && is_twice &&
                    !isCheck(opponent, this.ID, new Point(y++, x++, true), current_board))      // check if un poissant is possible
                moves.add(new Point(5, x++, true));

            if(this.colour == 0 && board[4][x--].equals("Pawn-1") && place.getY() == 4 && x-- >= 0  && is_twice &&
                    !isCheck(opponent, this.ID, new Point(y++, x--, true), current_board))     // if the player is white, opponent is black and a pawn, whit is in the right placement and opponent just moved twice
                moves.add(new Point(5, x--, true));
        }

//_________________________________Black________________________________________

        if(y-- >= 0) {

            if(moved == 0 && this.colour == 1 && isEmpty(board, 5, x) &&
                    !isCheck(opponent, this.ID, new Point(y--, x), current_board)){  // if this is the pawn's first move and the player is black, check if it can move forward once and twice.
                moves.add(new Point(y--, x));
                if(isEmpty(board, y-2, x) && !isCheck(opponent, this.ID, new Point(y-2, x), current_board))
                    moves.add(new Point(y-2, x, false, true));
            }

            if (this.colour == 1 && isEmpty(board, y--, x) &&
                    !isCheck(opponent, this.ID, new Point(y--, x), current_board))  // check if it can move forward once as black.
                moves.add(new Point(y--, x));


            if (this.colour == 1 && x++ < 8 && !isEmpty(board, y--, x++)  &&  !isComrade(board, y--, x++) &&
                    !isCheck(opponent, this.ID, new Point(y--, x++, true), current_board))        // check if black can take a piece
                moves.add(new Point(y--, x++, true));


            if (this.colour == 1 && x-- >= 0 && !isEmpty(board, y--, x--) && !isComrade(board, y--, x--) &&
                    !isCheck(opponent, this.ID, new Point(y--, x--, true), current_board))
                moves.add(new Point(y--, x--, true));


            if(this.colour == 1 && place.getY() == 3 && x++ < 8 && board[3][x++].equals("Pawn-0") && is_twice &&
                    !isCheck(opponent, this.ID, new Point(2, x++, true), current_board))      // check if un poissant is possible
                moves.add(new Point(2, x++, true));

            if(this.colour == 1 && place.getY() == 3 && x-- >= 0 && board[3][x--].equals("Pawn-0") && is_twice &&
                    !isCheck(opponent, this.ID, new Point(2, x--, true), current_board))
                moves.add(new Point(2, x--, true));


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

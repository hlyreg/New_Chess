package com.example.new_chess.pieces;

import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece{

    public Knight(Point place, int colour, Player player, int ID){
        super(place, colour, player, ID);
    }

    @Override
    public List<Point> getMoves(Board current_board, boolean trigger, Player opponent) {
        int x = this.place.getX();
        int y = this.place.getY();
        Piece[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();
//__________________________________________________________________________________________
        for(int num = -2; num < 3; num+=4){     // will run twice, once num = 2, once num = -2

            if(y+num < 8 && y+num >= 0){         // when the knight moves vertically, when num = 2, go up, and when it's -2 go down

                if(x-1 >= 0){
                    if(isEmpty(board, x-1, y+num) )
                        moves.add(new Point(x-1, y+num));

                    if(!isEmpty(board, x-1, y+num) && !isComrade(board, x-1, y+num) )
                        moves.add(new Point(x-1, y+num, true));     // does the move kill a piece
                }
                if(x+1 < 8){
                    if(isEmpty(board, x+1, y+num) )
                        moves.add(new Point(x+1, y+num));

                    if(!isEmpty(board, x+1, y+num) && !isComrade(board, x+1, y+num) )
                        moves.add(new Point(x+1, y+num, true));
                }
            }
            if(x+num < 8 && x+num >= 0){        //when the knight moves horizontally, when num = 2, go right, and when it's -2 go left
                if(y-1 >= 0){
                    if(isEmpty(board, x+num, y-1) )
                        moves.add(new Point(x+num, y-1));

                    if(!isEmpty(board, x+num, y-1) && !isComrade(board, x+num, y-1) )
                        moves.add(new Point(x+num, y-1, true));
                }
                else if(y+1 < 8){
                    if(isEmpty(board, x+num, y+1) )
                        moves.add(new Point(x+num, y+1));

                    if(!isEmpty(board, x+num, y+1) && !isComrade(board, x+num, y+1))
                        moves.add(new Point(x+num, y+1, true));
                }
            }

        }
        return moves;
    }

    @Override
    public void move(Point newPlace) {
        this.place = newPlace;
    }
}


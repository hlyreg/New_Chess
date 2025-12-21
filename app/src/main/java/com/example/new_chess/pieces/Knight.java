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
        String[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();
//__________________________________________________________________________________________
        for(int num = -2; num < 3; num+=4){     // will run twice, once num = 2, once num = -2

            if(y+num < 8 && y+num >= 0){         // when the knight moves vertically, when num = 2, go up, and when it's -2 go down

                if(x-- >= 0){
                    if(isEmpty(board, y+num, x--) &&
                            !isCheck(opponent, this.ID, new Point(y+num, x--), current_board))
                        moves.add(new Point(y+num, x--));

                    else if(!isEmpty(board, y+num, x--) && !isComrade(board, y+num, x--) &&
                            !isCheck(opponent, this.ID, new Point(y+num, x--, true), current_board))
                        moves.add(new Point(y+num, x--, true));     // does the move kill a piece
                }
                else if(x++ < 8){
                    if(isEmpty(board, y+num, x++) &&
                            !isCheck(opponent, this.ID, new Point(y+num, x++), current_board))
                        moves.add(new Point(y+num, x++));

                    else if(!isEmpty(board, y+num, x++) && !isComrade(board, y+num, x++) &&
                            !isCheck(opponent, this.ID, new Point(y+num, x++, true), current_board))
                        moves.add(new Point(y+num, x++, true));
                }
            }
            if(x+num < 8 && x+num >= 0){        //when the knight moves horizontally, when num = 2, go right, and when it's -2 go left
                if(y-- >= 0){
                    if(isEmpty(board, y--, x+num) &&
                            !isCheck(opponent, this.ID, new Point(y--, x+num), current_board))
                        moves.add(new Point(y--, x+num));

                    else if(!isEmpty(board, y--, x+num) && !isComrade(board, y--, x+num) &&
                            !isCheck(opponent, this.ID, new Point(y--, x+num, true), current_board))
                        moves.add(new Point(y--, x+num, true));
                }
                else if(y++ < 8){
                    if(isEmpty(board, y++, x+num) &&
                            !isCheck(opponent, this.ID, new Point(y++, x+num), current_board))
                        moves.add(new Point(y++, x+num));

                    else if(!isEmpty(board, y++, x+num) && !isComrade(board, y++, x+num)&&
                            !isCheck(opponent, this.ID, new Point(y++, x+num, true), current_board))
                        moves.add(new Point(y++, x+num, true));
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


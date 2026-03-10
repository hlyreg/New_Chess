package com.example.new_chess.pieces;
import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece{

    public Queen(Point place, int colour, Player player, int ID){
        super(place, colour, player, ID);
    }

    public List<Point> getMoves(Board current_board, boolean trigger, Player opponent) {
        Piece[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();


        // check if a move can be made for every combination of -1, 0, and 1
        for(int height = -1; height < 2; height++) {
            for (int width = -1; width < 2; width++) {
                moves.addAll(this.continueDirection(width, height, current_board));
            }
        }

        return moves;
    }

    @Override
    public void move(Point newPlace) {
        this.place = newPlace;
    }
}

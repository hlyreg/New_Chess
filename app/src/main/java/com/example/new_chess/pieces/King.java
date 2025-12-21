package com.example.new_chess.pieces;

import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece{
    private boolean moved; // for casting

    public King(Point place, int colour, Player player, int ID){
        super(place, colour, player, ID);
        this.moved = false;
    }

    @Override
    public List<Point> getMoves(Board current_board, boolean trigger, Player opponent) {
        int x = this.place.getX();
        int y = this.place.getY();
        String[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();
        int tryY; int tryX;


        // check if a move can be made for every combination of -1, 0, and 1
        for(int height = -1; height < 2; height++){
            for(int width = -1; width < 2; width++){
                tryY = y+height;
                tryX = x+width;
                if(tryX < 8 && tryY < 8 && tryX >= 0 && tryY >= 0 && isEmpty(board, tryY, tryX) &&
                        !isCheck(opponent, this.ID, new Point(tryY, tryX), current_board)){
                    moves.add(new Point(tryY, tryX));
                }
            }
        }
        List<Point> right = this.continueDirection(1,0, current_board, opponent);
        List<Point> left = this.continueDirection(-1,0, current_board, opponent);

        if(!this.moved && this.player.getPieces()[13] instanceof Rook && !((Rook) this.player.getPieces()[13]).getMoved() &&
                right.get((right.toArray().length-1)).compare(this.place.getY(), 6) &&!right.get((right.toArray().length-1)).isTakes())

            moves.add(new Point(this.place.getY(), 6, false, false, true));


        if(!this.moved && this.player.getPieces()[8] instanceof Rook && !((Rook) this.player.getPieces()[13]).getMoved() &&
                left.get((left.toArray().length-1)).compare(this.place.getY(), 1) &&!left.get((left.toArray().length-1)).isTakes())

            moves.add(new Point(this.place.getY(), 1, false, false, true));



        return moves;
    }

    @Override
    public void move(Point newPlace) {
        this.place = newPlace;
    }

}

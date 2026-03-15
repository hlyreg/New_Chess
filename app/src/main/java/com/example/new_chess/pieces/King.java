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

    public King(King other, Player player){
        super(other, player);
        this.moved = other.moved;
    }

    @Override
    public List<Point> getMoves(Board current_board, boolean trigger, Player opponent) {
        int x = this.place.getX();
        int y = this.place.getY();
        Piece[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();
        int tryY; int tryX;


        // check if a move can be made for every combination of -1, 0, and 1
        for(int height = -1; height < 2; height++){
            for(int width = -1; width < 2; width++){
                tryY = y+height;
                tryX = x+width;
                if(tryX < 8 && tryY < 8 && tryX >= 0 && tryY >= 0){
                    if(isEmpty(board, tryX, tryY))
                        moves.add(new Point(tryX, tryY));
                    else if(!isComrade(board, tryX, tryY ))
                        moves.add(new Point(tryX, tryY, true));

                }
            }
        }

        return moves;
    }

    @Override
    public Piece copy(Player player) {
        return new King(this, player);
    }


    public boolean getMoved(){
        return this.moved;
    }

    public void move(Point move) {
        this.place = new Point(move);

        if (move.isCastling()) {
            // King-side
            if (move.getX() == 6) {
                Rook rook = (Rook) this.player.getPieces()[15];
                rook.move(new Point(5, move.getY()));
            }
            // Queen-side
            else if (move.getX() == 2) {
                Rook rook = (Rook) this.player.getPieces()[8];
                rook.move(new Point(3, move.getY()));
            }
        }
        moved = true;
    }



}

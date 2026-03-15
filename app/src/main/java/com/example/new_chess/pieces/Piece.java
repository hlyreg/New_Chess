package com.example.new_chess.pieces;

import com.example.new_chess.game.Board;
import com.example.new_chess.game.Player;
import com.example.new_chess.game.Point;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {
    protected Point place;
    protected int colour;  // player = 0 : white, player = 1 : black.
    protected int ID;
    protected Player player;
    protected boolean isAlive;

    protected Piece(Point place, int colour, Player player, int ID){
        this.place = place;
        this.player = player;
        this.colour = colour;
        this.ID = ID;
        this.isAlive = true;

    }

    protected Piece(Piece other, Player player){
        this.place = other.place;
        this.player = player;
        this.colour = other.colour;
        this.ID = other.ID;
        this.isAlive = other.isAlive;
    }

    public abstract List<Point> getMoves(Board board, boolean trigger, Player opponent);

    public abstract Piece copy(Player player);  // for copy we need to reinstate what player it is connected to because they are constantly being remade
    public abstract void move(Point newPlace);

    public void kill(){
        this.isAlive = false;
    }

    public boolean isEmpty(Piece[][] board, int x, int y){
        return board[x][y] == null;
    }

    public boolean isComrade(Piece[][] board, int x, int y){

        if(board[x][y] != null){
            int my_player = this.colour;
            Piece piece = board[x][y];
            return my_player == piece.getColour();
        }
        return false;
    }

    public boolean isCheck(Player opponent, int id, Point move, Board board){
        Piece moving = player.getPieces()[id];
        Board newBoardCheck = new Board(board, moving.getPlace(), move, moving); // simulate the move

        // Find your king's position after the move
        Point kingPlace = player.getPieces()[12].getPlace();
        if (moving instanceof King) {
            kingPlace = move; // king moves, its new location is move
        }

        return newBoardCheck.isSquareAttacked(kingPlace.getY(), kingPlace.getX(), newBoardCheck.getPlayer(opponent.getColour()));
    }
    public List<Point> getLegalMoves(Board board, boolean two_step) {
        List<Point> legalMoves = new ArrayList<>();

        Player opponent = board.getOpponent(this);

        List<Point> moves = this.getMoves(board, two_step, opponent);

        for (Point move : moves) {

            if (!this.isCheck(opponent, this.ID, move, board)) {
                legalMoves.add(move);
            }
        }

        if (this instanceof King) {
            King king = (King) this;

            if (!king.getMoved() && !king.isCheck(board.getOpponent(king), king.getID(), king.getPlace(), board)) {

                // King-side
                if (board.canCastleKingSide(king)) {
                    legalMoves.add(new Point(6, king.getPlace().getY(), false, false, true));
                }

                // Queen-side
                if (board.canCastleQueenSide(king)) {
                    legalMoves.add(new Point(2, king.getPlace().getY(), false, false, true));
                }
            }
        }


        return legalMoves;
    }
    protected List<Point> continueDirection(int deltaX, int deltaY, Board current_board){      // a function that return the amount of places available, when going in a certain direction
        int x = this.place.getX();
        int y = this.place.getY();
        Piece[][] board = current_board.getBoard();
        List<Point> moves = new ArrayList<>();

        // delta x and y represent the distance being made, fore example - (1, 1) will head to the top right.
        int tryX = x + deltaX;
        int tryY = y + deltaY;
        //run till you hit something (edge or piece)
        while(tryX < 8 && tryY < 8 && tryX >= 0 && tryY >= 0 && isEmpty(board, tryX, tryY)){
            moves.add(new Point(tryX, tryY));
            tryX += deltaX;
            tryY += deltaY;
        }
        // if it's not an edge that was hit, check if it is an opponent piece that can be taken
        if(tryX < 8 && tryY < 8 && tryX >= 0 && tryY >= 0 && !isComrade(board, tryX, tryY))
            moves.add(new Point(tryX, tryY, true));

        return moves;
    }

    public Point getPlace(){
        return this.place;
    }

    public int getColour(){
        return this.colour;
    }

    public boolean isAlive(){
        return this.isAlive;
    }

    public int getID(){
        return this.ID;
    }

    public Player getPlayer(){
        return this.player;
    }
}


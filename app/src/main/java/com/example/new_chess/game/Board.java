package com.example.new_chess.game;

import com.example.new_chess.pieces.King;
import com.example.new_chess.pieces.Piece;
import com.example.new_chess.pieces.Rook;

import java.util.List;

public class Board {
    private Piece[][] board;
    private Player black;
    private Player white;
    private Point lastPawnDoubleStep; // null if none

    public Board(Player white, Player black){
        this.board = new Piece[8][8];
        Piece[] whitePieces = white.getPieces();
        Piece[] blackPieces = black.getPieces();


        for( int x = 0; x < 8; x++) {
            board[x][6] = whitePieces[x];
            board[x][1] = blackPieces[x];

            if(x % 7 == 0) {
                board[x][7] = whitePieces[x+8];
                board[x][0] = blackPieces[x+8];
            }
            else if (x == 1 || x == 6) {
                board[x][7] = whitePieces[x+8];
                board[x][0] = blackPieces[x+8];
            }
            else if (x == 2 || x == 5) {
                board[x][7] = whitePieces[x+8];
                board[x][0] = blackPieces[x+8];
            }
        }
        board[3][7] = whitePieces[11];
        board[3][0] = blackPieces[11];
        board[4][7] = whitePieces[12];
        board[4][0] = blackPieces[12];

        this.white = white;
        this.black = black;
    }

    public Board(Board current_board, Point current, Point change, Piece moving){    // a constructor to make a new board with a change in position because of a move that was made
        this.board =  new Piece[8][8];
        Piece[][] currentBoard = current_board.getBoard();
        for(int y = 0; y < 8; y++){    // copying the original board
            for(int x = 0; x < 8; x++){
                this.board[x][y] = currentBoard[x][y];
            }
        }

        if (change.isCastling()) {
            if(moving.getColour() == 0) {
                if (change.getX() == 6) { // king-side
                    white.getPieces()[15].move(new Point(5, 7)); // move the rook
                } else { // queen-side
                    white.getPieces()[8].move(new Point(3, 7));
                }
            }
            else{
                if (change.getX() == 6) { // king-side
                    black.getPieces()[15].move(new Point(5, 0));
                } else { // queen-side
                    black.getPieces()[8].move(new Point(3, 0));
                }

            }
        }

        if(change.isPawn_twice()){
            lastPawnDoubleStep = change;
        }
        else{
            lastPawnDoubleStep = null;
        }
        // adding the change in position
        this.board[change.getX()][change.getY()] = this.board[current.getX()][current.getY()];
        this.board[current.getX()][current.getY()] = null;
    }

    public boolean wasLastMovePawnTwo() {
        return lastPawnDoubleStep != null;
    }

    public Piece[][] getBoard(){
        return this.board;
    }

    public Piece findPiece(Point point ){
        return board[point.getX()][point.getY()];
    }

    public Board makeMove(Piece piece, Point move) {
        // capture
        Piece target = findPiece(move);
        if (target != null) {
            target.kill();
        }

        Board new_board = new Board(this, piece.getPlace(), move, piece);

        piece.move(move);
        return new_board;
    }


    public Player getOpponent(Piece piece){
        int colour = piece.getColour();
        if(colour == 0)
            return black;
        return white;
    }

    public Player getPlayer(int colour) {
        if(colour ==0)
            return white;
        return black;
    }

    public boolean canCastleKingSide(King king) {

        int y = king.getPlace().getY();
        int x = king.getPlace().getX(); // should be 4

        // 1. King must not have moved
        if (king.getMoved())
            return false;

        Player player = king.getPlayer();
        Player opponent = getOpponent(king);

        // 2. Rook must exist and not have moved
        Piece rookPiece = player.getPieces()[15];
        if (!(rookPiece instanceof Rook))
            return false;

        Rook rook = (Rook) rookPiece;
        if (rook.getMoved())
            return false;

        // 3. Squares between king and rook must be empty (f and g)
        if (board[5][y]!=null || board[6][y] != null)
            return false;

        // 4. King must NOT be in check now
        if (isSquareAttacked(y, x, opponent))
            return false;

        // 5. King must NOT pass through check (f square)
        if (isSquareAttacked(y, 5, opponent))
            return false;

        // 6. King must NOT land in check (g square)
        if (isSquareAttacked(y, 6, opponent))
            return false;

        return true;
    }

    public boolean canCastleQueenSide(King king) {

        int y = king.getPlace().getY();
        int x = king.getPlace().getX(); // should be 4

        // 1. King must not have moved
        if (king.getMoved())
            return false;

        Player player = king.getPlayer();
        Player opponent = getOpponent(king);

        // 2. Queen-side rook must exist and not have moved
        Piece rookPiece = player.getPieces()[8];
        if (!(rookPiece instanceof Rook))
            return false;

        Rook rook = (Rook) rookPiece;
        if (rook.getMoved())
            return false;

        // 3. Squares between king and rook must be empty
        // b, c, d squares must be empty
        if (board[1][y] != null ||
                board[2][y]!=null ||
                board[3][y]!=null)
            return false;

        // 4. King must NOT be in check now
        if (isSquareAttacked(y, x, opponent))
            return false;

        // 5. King must NOT pass through check (d square)
        if (isSquareAttacked(y, 3, opponent))
            return false;

        // 6. King must NOT land in check (c square)
        if (isSquareAttacked(y, 2, opponent))
            return false;

        return true;
    }

    public boolean isSquareAttacked(int y, int x, Player opponent) {
        // go over all the opponents pieces
        for (Piece piece : opponent.getPieces()) {
            if (!piece.isAlive())  //skip if piece isn't alive
                continue;

            // go over all the opponents moves
            List<Point> moves = piece.getMoves(this, false, getOpponent(piece));

            for (Point move : moves) {
                if (move.compare(x, y))
                    return true;
            }
        }
        return false;
    }

}


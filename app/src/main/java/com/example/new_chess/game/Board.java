package com.example.new_chess.game;
public class Board {
    private String[][] board;

    public Board(){
        this.board = new String[8][8];

        for(int i = 0; i < 8; i++){    // "-" = no piece, making sure all empty spots are "empty" and note null
            for( int j = 0; j < 8; j++){
                board[i][j] = "-";
            }
        }

        for( int x = 0; x < 8; x++) {
            board[1][x] = "Pawn-0";
            board[6][x] = "Pawn-1";

            if(x % 7 == 0) {
                board[0][x] = "Rook-0";
                board[7][x] = "Rook-1";
            }
            else if (x == 1 || x == 6) {
                board[0][x] = "Knight-0";
                board[7][x] = "Knight-1";
            }
            else if (x == 2 || x == 5) {
                board[0][x] = "Bishop-0";
                board[7][x] = "Bishop-1";
            }
        }
        board[0][3] = "Queen-0";
        board[7][3] = "Queen-1";
        board[0][4] = "King-0";
        board[7][4] = "King-1";
    }

    public Board(Board current_board, Point current, Point change){    // a constructor to make a new board with a change in position because of a move that was made
        for(int y = 0; y < 8; y++){    // copying the original board
            for(int x = 0; x < 8; x++){
                this.board[y][x] = current_board.getBoard()[y][x];
            }
        }
        // adding the change in position
        this.board[change.getY()][change.getX()] = this.board[current.getY()][current.getX()];
        this.board[current.getY()][current.getX()] = "-";
    }

    public String[][] getBoard(){
        return this.board;
    }
}


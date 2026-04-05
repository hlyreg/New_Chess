package com.example.new_chess.firebase;

public class GameResult {
    public String type; // "win", "draw", "resign"
    public int loser;   // 0 = white, 1 = black, -1 for draw

    public GameResult() {
        // Needed for Firebase
    }

    public GameResult(String type, int loser) {
        this.type = type;
        this.loser = loser;
    }
}

package com.example.new_chess.firebase;

public class User {
    public String username;
    public String email;
    public String password;
    public int wins;
    public int losses;
    public int draws;

    // Required empty constructor for Firebase
    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        wins = 0;
        losses = 0;
        draws = 0;
    }
}

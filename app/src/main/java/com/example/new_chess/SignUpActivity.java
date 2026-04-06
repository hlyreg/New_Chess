package com.example.new_chess;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.new_chess.firebase.ThemeManager;
import com.example.new_chess.firebase.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText ETpassword;
    private EditText ETemail;
    private TextView TVerror;
    private TextView ETusername;
    private FirebaseAuth auth;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ThemeManager.applyTheme(
                findViewById(android.R.id.content),
                ThemeManager.getTheme(this)
        );
        ETpassword = findViewById(R.id.IDInput);
        ETemail = findViewById(R.id.nameInput);
        ETusername = findViewById(R.id.UsernameInput);
        TVerror =findViewById(R.id.errorTextView);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
    }

    public void createUser(View view){
        String email = ETemail.getText().toString();
        String username = ETusername.getText().toString();
        String password = ETpassword.getText().toString();
        if(email.isEmpty()||password.isEmpty()){
            TVerror.setText("please fill all fields");
        }
        else{
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Connecting...");
            pd.setMessage("creating user...");
            pd.show();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    pd.dismiss();
                    if(task.isSuccessful()){

                        String uid = auth.getCurrentUser().getUid();

                        User user = new User(username, email);

                        database.child("users")
                                .child(uid)
                                .setValue(user);

                        startActivity(new Intent(SignUpActivity.this, StartActivity.class));
                        finish();
                    }
                    else{
                        Exception exp = task.getException();
                        if(exp instanceof FirebaseAuthInvalidUserException){
                            TVerror.setText("invalid email address.");
                        }
                        else if(exp instanceof FirebaseAuthWeakPasswordException){
                            TVerror.setText("Password too weak.");
                        }
                        else if(exp instanceof FirebaseAuthUserCollisionException){
                            TVerror.setText("User already exists.");
                        }
                        else if(exp instanceof FirebaseAuthInvalidCredentialsException){
                            TVerror.setText("General authentication failure.");
                        }
                        else if(exp instanceof FirebaseNetworkException){
                            TVerror.setText("Network error. please check your connection.");
                        }
                        else{
                            TVerror.setText("An error occured. please try again later.");
                        }

                    }
                }
            });
        }


    }

    public void LogIn(View view) {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        SignUpActivity.this.startActivity(intent);

    }

}

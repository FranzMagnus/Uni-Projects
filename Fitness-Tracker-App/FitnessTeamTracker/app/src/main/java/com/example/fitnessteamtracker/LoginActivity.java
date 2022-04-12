package com.example.fitnessteamtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();

        if(fbUser==null) {
            final Button signIn = findViewById(R.id.signinButton);
            final Button register = findViewById(R.id.register_Button);
            final TextView textView = findViewById(R.id.textView);

            final EditText emailText = findViewById(R.id.emailText);
            final EditText passwordText = findViewById(R.id.passwordText);

            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn(emailText.getText().toString(), passwordText.getText().toString());
                }
            });

            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        }else{
            Intent intent = new Intent(LoginActivity.this, MenuActivity2.class);
            startActivity(intent);
            Log.d(TAG, "signedInUser:"+ fbUser);
            Toast.makeText(LoginActivity.this, "Signed in" + fbUser, Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "loggedInUser: success");
                    Intent intent = new Intent(LoginActivity.this, MenuActivity2.class);
                    startActivity(intent);
                } else{
                    Log.d(TAG, "loggedInUser: no success");
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

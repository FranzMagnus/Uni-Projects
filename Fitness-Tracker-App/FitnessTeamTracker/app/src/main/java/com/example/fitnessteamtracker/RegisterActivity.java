package com.example.fitnessteamtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessteamtracker.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.example.fitnessteamtracker.Util.HttpPoster;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        final EditText nameText = (EditText) findViewById(R.id.nameTextRegister);
        final EditText emailText = (EditText) findViewById(R.id.emailTextRegister);
        final EditText passwordText = (EditText) findViewById(R.id.passwordTextRegister);
        final Button register = findViewById(R.id.registerButtonRegister);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register(nameText.getText().toString(), emailText.getText().toString(), passwordText.getText().toString());
            }
        });
    }

    private void register(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "createdUser: success");
                    User u = new User(FirebaseAuth.getInstance().getUid(), name);
                    new HttpPoster(u).execute("insert", "user");
                    Intent intent = new Intent(RegisterActivity.this, MenuActivity2.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "createdUser: no success", task.getException());
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

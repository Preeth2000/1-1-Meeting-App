package com.example.videocallapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.videocallapp.R;
import com.example.videocallapp.utilities.Constants;
import com.example.videocallapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private EditText inputFirstName, inputLastName, inputEmail, inputPassword, inputConfirmPassword;
    private MaterialButton buttonSignUp;
    private ProgressBar signupprogress;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        preferenceManager = new PreferenceManager(getApplicationContext());
        findViewById(R.id.backarrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.textSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputFirstName       = findViewById(R.id.inputFirstName);
        inputLastName        = findViewById(R.id.inputLastName);
        inputEmail           = findViewById(R.id.inputEmailsignup);
        inputPassword        = findViewById(R.id.inputPasswordsignup);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonSignUp         = findViewById(R.id.buttonSignUp);
        signupprogress       = findViewById(R.id.signupprogress);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputFirstName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter first name", Toast.LENGTH_SHORT).show();
                } else if (inputLastName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter last name", Toast.LENGTH_SHORT).show();
                } else if (inputEmail.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter email", Toast.LENGTH_SHORT).show();
                    //Do I need this One Below
                } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {
                    Toast.makeText(SignUp.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                } else if (inputPassword.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter password", Toast.LENGTH_SHORT).show();
                } else if (inputConfirmPassword.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUp.this, "Confirm your password", Toast.LENGTH_SHORT).show();
                } else if (!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())) {
                    Toast.makeText(SignUp.this, "Password & confirm password must be same", Toast.LENGTH_SHORT).show();
                } else {
                    signUp();
                    }
            }
        });

    }

    private void signUp() {

        buttonSignUp.setVisibility(View.INVISIBLE);
        signupprogress.setVisibility(View.VISIBLE);

        FirebaseFirestore database    = FirebaseFirestore.getInstance();
        HashMap<String, Object> users = new HashMap<>();
        users.put(Constants.KEY_FIRST_NAME, inputFirstName.getText().toString());
        users.put(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
        users.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
        users.put(Constants.KEY_PASSWORD, inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(users)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_FIRST_NAME, inputFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.getText().toString());
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    //
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signupprogress.setVisibility(View.INVISIBLE);
                        buttonSignUp.setVisibility(View.VISIBLE);
                        Toast.makeText(SignUp.this, "There has been a problem : "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        }
}
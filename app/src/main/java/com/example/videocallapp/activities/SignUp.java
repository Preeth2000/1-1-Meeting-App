package com.example.videocallapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
    private MaterialButton signUpButton;
    private ProgressBar signUpProgress;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets new layout created for sign up activity
        setContentView(R.layout.activity_sign_up);

        preferenceManager = new PreferenceManager(getApplicationContext());

        //Waits for back arrow button to be pressed
        //If pressed, redirects users back to sign in page
        findViewById(R.id.imageBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //Waits for sign in button to be pressed
        //If pressed also redirects user to sign in page
        findViewById(R.id.signInRouter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Sets store for all data that can be input or changed by user
        inputFirstName = findViewById(R.id.inputFirstName);
        inputLastName = findViewById(R.id.inputLastName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        signUpButton = findViewById(R.id.signUpButton);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        signUpProgress = findViewById(R.id.signUpProgress);

        //Waits for sign in button to be pressed
        //If pressed checks validation of data input in ech field
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputFirstName.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUp.this, "Enter first name", Toast.LENGTH_SHORT).show();
                }else if (inputLastName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter last name", Toast.LENGTH_SHORT).show();
                }else if (inputEmail.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter email", Toast.LENGTH_SHORT).show();
                }//Checks that email is in correct format
                 else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                    Toast.makeText(SignUp.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                }else if (inputPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUp.this, "Enter password", Toast.LENGTH_SHORT).show();
                }else if (inputConfirmPassword.getText().toString().trim().isEmpty()){
                    Toast.makeText(SignUp.this, "Re-enter your password", Toast.LENGTH_SHORT).show();
                }//Checks that both passwords entered by user are the same
                 else if (!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())){
                    Toast.makeText(SignUp.this, "Passwords must be same", Toast.LENGTH_SHORT).show();
                }else {
                    //Calls Sign Up function
                    signUp();
                }
            }
        });

    }

    //Function to sign user into application
    protected void signUp() {
        //Replaces sign up button with loading animation to to communicate to user that sign up request is being processed
        signUpButton.setVisibility(View.INVISIBLE);
        signUpProgress.setVisibility(View.VISIBLE);

        //Instantiates database from cloud storage
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //Creates a hash-map to store all user details input as single entry
        HashMap<String, Object> user = new HashMap<>();

        //Converts all data needed for a user account to string to be processed in the correct format
        user.put(Constants.KEY_FIRST_NAME, inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, inputPassword.getText().toString());

        //Finds all users in database and add new user to list
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    //If user is successfully added to database
                    //Shared preferences updated to remember the new user data
                    //This allows them to stay signed in
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, inputFirstName.getText().toString());
                        preferenceManager.putString(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
                        preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.getText().toString());
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    //If sign up is unsuccessful, loading animation replaced with sign up button as before
                    //User informed that they are unable to be signed up
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        signUpProgress.setVisibility(View.INVISIBLE);
                        signUpButton.setVisibility(View.VISIBLE);
                        Toast.makeText(SignUp.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
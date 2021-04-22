package com.example.videocallapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.videocallapp.R;
import com.example.videocallapp.utilities.Constants;
import com.example.videocallapp.utilities.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;


public class SignIn extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private MaterialButton signInButton;
    private ProgressBar signInProgress;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets new layout created for sign in activity
        setContentView(R.layout.activity_sign_in);

        //Checks if user has already been logged in in shared preferences
        //If so then redirects user to the main activity page
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        //Adding a test user to database
        //User added on application start up
        //Adding all required user information
        //No user key as this user cannot be called
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put("first_name", "John");
        user.put("last_name", "Smith");
        user.put("email", "john_smith@example.com");
        user.put("password", "secure_password77");
        database.collection("users")
                .add(user)
                //If data is successfully added to database, logcat will output that the test is a success
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("SUS", "User successfully logged in ");
                    }
                })
                //If data is not added to database, logcat will output that the test is a failure
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("SUS", "Unable to add User");
                    }
                });

        //Testing whether test user can be signed in
        //Passing required test user data that was sent to database in code above
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, "john_smith@example.com")
                .whereEqualTo(Constants.KEY_PASSWORD, "secure_password77")
                .get()
                //Listens for response from database
                //If the above details can be found in database, then same function used to sign in a normal user used
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //Checks if user is successfully found and that some value has been found in database
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            //If user is found, then they can be logged in which will then be output in logcat
                            Log.d("SIS", "User successfully logged in ");
                        }else {
                            //If user cannot be found then they cannot be logged in which will be output in logcat
                            Log.d("SIS", "User cannot be logged in ");
                        }
                    }
                });

        //Waits for user to click on sign up button. When pressed, redirects user to sigh up activity page
        findViewById(R.id.signUpRouter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUp.class));
            }
        });

        //Storing email and password input in respective fields on sign in page
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        //Storing states of sign in and sign up buttons. On change to state a new process will be run
        signInButton = findViewById(R.id.signInButton);
        signInProgress = findViewById(R.id.signInProgress);

        //Waits for sign in button to be pressed. On doing so validates user input
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Ensures an email is input by user, email is a valid email and that password has been input as well
                if (inputEmail.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignIn.this, "Enter email", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {
                    Toast.makeText(SignIn.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                } else if (inputPassword.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignIn.this, "Enter password", Toast.LENGTH_SHORT).show();
                } else {
                    //If all validation criteria is met, then Sign in function is called
                    SignIn.this.signIn();
                }
            }
        });

    }

    //Sign in Function
    protected void signIn(){
        //Replaces sign in button with loading animation to communicate to user that sign in request is being processed
        signInButton.setVisibility(View.INVISIBLE);
        signInProgress.setVisibility(View.VISIBLE);

        //Instantiates database from cloud storage
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //Search through all users to find match for the input email an password
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //If user details are correct and details are confirmed to be in the database, user is signed in
                        //Shared preferences updated to reflect this, all user attributes updated with current user information
                        //This allows them to stay signed in
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                            preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                            //User redirected to main activity page
                            Intent intent = new Intent(SignIn.this.getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            SignIn.this.startActivity(intent);
                        } else {
                            //If sign in is unsuccessful, loading animation replaced with sign in button as before
                            //User informed that they are unable to be signed in
                            signInProgress.setVisibility(View.INVISIBLE);
                            signInButton.setVisibility(View.VISIBLE);
                            Toast.makeText(SignIn.this, "Unable to Sign In", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

}

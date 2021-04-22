package com.example.videocallapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.videocallapp.R;
import com.example.videocallapp.adapters.UsersAdapter;
import com.example.videocallapp.listeners.UsersListener;
import com.example.videocallapp.models.User;
import com.example.videocallapp.utilities.Constants;
import com.example.videocallapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListener {

    private PreferenceManager preferenceManager;
    private List<User> users;
    private UsersAdapter usersAdapter;
    private TextView errorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets new layout created for main activity
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(getApplicationContext());

        //Sets user first and last name as title on Main screen layout
        TextView title = findViewById(R.id.title);
        title.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        //Maps event of pressing on sign out button on Main screen layout to sign out function
        findViewById(R.id.signOutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        //Gets user token and sends value to database when user enters main activity by calling sendFCMTokenToDatabase method
        //Ensures only logged in users have tokens and so are the only ones who can be called
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() != null){
                    sendFCMTokenToDatabase(task.getResult().getToken());
                }
            }
        });

        //Sets view from main activity layout to set user list into
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        errorMessage = findViewById(R.id.errorMessage);

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        recyclerView.setAdapter(usersAdapter);

        //Adds swipe refresh animation that searches and returns users when called
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        getUsers();
    }

    //Method accesses database to get a list of all users in database
    private void getUsers() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        swipeRefreshLayout.setRefreshing(false);
                        String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                        if (task.isSuccessful() && task.getResult() != null) {
                            //clears list to then repopulate list from scratch
                            //Removes chances for duplicate records
                            users.clear();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                //Removes current user ID from list so they are not given option to call themselves
                                if (myUserId.equals(documentSnapshot.getId())){
                                    continue;
                                }
                                //Stores data about each user found in database
                                User user = new User();
                                user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                                user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                                user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                                user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                users.add(user);
                                Log.d("USA", "User: " + " " + user + " " + user.firstName + " " + user.lastName + " " + user.email + " " + "token: " + user.token);
                            }
                            if (users.size() > 0) {
                                usersAdapter.notifyDataSetChanged();
                            }
                            else {
                                errorMessage.setText(String.format("%s ", "No users available"));
                                errorMessage.setVisibility(View.VISIBLE);
                            }
                        }else {
                            errorMessage.setText(String.format("%s ", "No users available"));
                            errorMessage.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    //Method that sends current logged in user's token to database
    private void sendFCMTokenToDatabase(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                //Finds entry for current user
                database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        //Adds token to user entry in database
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Could not update Token " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    //Method to allow users to sign out
    private void signOut(){
        //Outputs message to user
        Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
        //Finds user entry in database and deletes token so they cannot be called when logged out
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        //Delete token from user in database
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        preferenceManager.clearPreferences();
                        //Redirects to sign in page after user has signed out
                        startActivity(new Intent(getApplicationContext(), SignIn.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error - Unable to sign out", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Method to initiate a video call with another user
    @Override
    public void initiateVideoCall(User user) {
        //Checks if user has a token
        //If not then they are not logged in and so are not called
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    user.firstName + " " + user.lastName + " is unavailable",
                    Toast.LENGTH_SHORT
            ).show();
        }else {
            //Sets meeting type to video and passes value to the Call Activity
            Intent intent = new Intent(getApplicationContext(), OutgoingCall.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
        }

    }

    //Method to initiate an audio call with another user
    @Override
    public void initiateAudioCall(User user) {
        //Checks if user has a token
        //If not then they are not logged in and so are not called
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    user.firstName + " " + user.lastName + " is unavailable",
                    Toast.LENGTH_SHORT
            ).show();
        }else {
            //Sets meeting type to audio and passes value to the Call Activity
            Intent intent = new Intent(getApplicationContext(), OutgoingCall.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }
}
package com.example.videocallapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videocallapp.R;
import com.example.videocallapp.adapters.UserAdapter;
import com.example.videocallapp.listener.UserListener;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserListener {

    private PreferenceManager preferenceManager;
    private List<User> users;
    private UserAdapter userAdapter;
    private TextView errormessage;
    private ProgressBar usersprogress;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(getApplicationContext());

        TextView textView = findViewById(R.id.title);
        textView.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        findViewById(R.id.signoutbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful() && task.getResult() != null) {
                    sendFCMTokenToDatabase(task.getResult().getToken());
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        errormessage = findViewById(R.id.errormessage);
        usersprogress = findViewById(R.id.usersprogress);

        users = new ArrayList<>();
        userAdapter = new UserAdapter(users, this);
        recyclerView.setAdapter(userAdapter);

        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        getUsers();

    }

    private void getUsers() {
        swipeRefreshLayout.setRefreshing(true);
        usersprogress.setVisibility(View.VISIBLE);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        usersprogress.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        String myuserid = preferenceManager.getString(Constants.KEY_USER_ID);
                        if(task.isSuccessful() && task.getResult() != null) {
                            //clears list after swipe refresh and repopulates list
                            users.clear();
                            for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                //This ensure the user does not come up on their own users list
                                //Ensure they cannot call themselves
                                if(myuserid.equals(documentSnapshot.getId())){
                                    continue;
                                }
                                User user = new User();
                                user.firstname = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                                user.lastname = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                                user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                                user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                users.add(user);
                            }
                            if(users.size()<0) {
                                userAdapter.notifyDataSetChanged();
                                errormessage.setText(String.format("%s", "No current users"));
                                errormessage.setVisibility(View.VISIBLE);
                            }
                        }else{
                            errormessage.setText(String.format("%s", "No current users"));
                            errormessage.setVisibility(View.VISIBLE);

                        }
                    }
                });
    }

    private void sendFCMTokenToDatabase(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        //Testing whether new generated token successfully updates from database or not
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Updated Token", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Could not update token" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void signOut(){
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID) );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        preferenceManager.clearPreferences();
                        startActivity(new Intent(getApplicationContext(), SignIn.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error - Unable to Sign Out", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void initiatevideocall(User user) {
        if(user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, user.firstname + " " + user.lastname + "is unavailable", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Starting Video Call with" + user.firstname + "" + user.lastname, Toast.LENGTH_SHORT).show();


        }
    }

    @Override
    public void initiateaudiocall(User user) {
        if(user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(this, user.firstname + " " + user.lastname + "is unavailable", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Starting Call with " + user.firstname + "" + user.lastname, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), Send_Invite.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }
}
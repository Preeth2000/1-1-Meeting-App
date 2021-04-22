package com.example.videocallapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videocallapp.R;
import com.example.videocallapp.models.User;
import com.example.videocallapp.network.ApiClient;
import com.example.videocallapp.network.ApiService;
import com.example.videocallapp.utilities.Constants;
import com.example.videocallapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingCall extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String meetingRoom = null;
    private String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets new layout created for outgoing calls
        setContentView(R.layout.activity_outgoing_call);

        //Store data from shared preferences
        preferenceManager = new PreferenceManager(getApplicationContext());

        //Outputs an image to remind user what type of call they have initiated, video or audio
        ImageView imageMeetingType = findViewById(R.id.callType);
        //Stores chosen meeting type, video or audio
        meetingType = getIntent().getStringExtra("type");

        //Checks if an invitation for a call has been made
        //If so then changes image to remind user they type of call
        if (meetingType != null){
            if (meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }else {
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        }

        //Used to store the user being called details
        TextView nameText = findViewById(R.id.nameText);
        TextView emailText = findViewById(R.id.emailText);

        //Finds user being called and sets their name and email to be shown to the caller
        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null){
            nameText.setText(String.format("%s %s", user.firstName, user.lastName));
            emailText.setText(user.email);
        }

        //Stores state of cancel call button
        //Waits until pressed, if pressed then cancels call
        ImageView cancelCallButton = findViewById(R.id.cancelCallButton);
        cancelCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null){
                    cancelCall(user.token);
                }
            }
        });

        //Searches database for token to connect call
        //Unique code allows for only chosen user to be called
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() != null){
                    inviterToken = task.getResult().getToken();
                    if (meetingType !=null && user !=null){
                        initiateCall(meetingType, user.token);
                    }
                }
            }
        });
    }

    //Function to initiate the call
    private void initiateCall(String meetingType, String receiverToken){
        try {

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            //All required current user and state data stored in constants to be retrieved
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            //Creates unique meeting room ID for Jitsi API to use based on current user ID
            meetingRoom =
                    preferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                            UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            //Stores data needed by API to be passed in body
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            //Calls sendRemoteMessage function to send call to recipient
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Function to send call to recipient
    private void sendRemoteMessage(String remoteMessageBody, String type) {
        //Sends header and body to API which then calls recipient
        ApiClient.getRetrofit().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                //If API is able to connect to user then recipient is called
                //If user cancels call, call will be cancelled
                //If API is unable to connect, call will be cancelled
                if (response.isSuccessful()){
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                        Toast.makeText(OutgoingCall.this, "Calling", Toast.LENGTH_SHORT).show();
                    }else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(OutgoingCall.this, "Call Cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else {
                    Toast.makeText(OutgoingCall.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Toast.makeText(OutgoingCall.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    //Function to allow user to cancel a outgoing call
    private void cancelCall(String receiverToken){
        try {
            //If user cancels a call, required response data is sent back to API to stop connecting call
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        }catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Function to call Jitsi API
    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Checks outgoing call status, whether it has been able to connect or if it has been cancelled
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null){
                //If invitation is acceptd by recipient, API is run
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    try {
                        URL serverURL = new URL("https://meet.jit.si");

                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        //Creates meeting room using unique meeting room key only given to meeting participants
                        builder.setRoom(meetingRoom);
                        //Checks meeting type and sets room properties as necessary
                        if (meetingType.equals("audio")){
                            builder.setVideoMuted(true);
                        }
                        //Launches API
                        JitsiMeetActivity.launch(OutgoingCall.this, builder.build());
                        finish();
                    } catch (Exception exception) {
                        Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    //If call is rejected by recipient, API not called and call ends
                    Toast.makeText(context, "Call Rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    //Starts Call, creates a link between users with receiver for API
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    //Ends Call, severs link between API and users
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}
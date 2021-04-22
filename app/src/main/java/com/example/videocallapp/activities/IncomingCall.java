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
import com.example.videocallapp.network.ApiClient;
import com.example.videocallapp.network.ApiService;
import com.example.videocallapp.utilities.Constants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingCall extends AppCompatActivity {

    private String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets new layout created for incoming calls
        setContentView(R.layout.activity_incoming_call);

        //Stores state and image corresponding to the state of the meeting initiated
        ImageView callType = findViewById(R.id.callType);
        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        //If meeting has been initiated, image to output to user the type of meeting changed as suitable
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                callType.setImageResource(R.drawable.ic_video);
            }else {
                callType.setImageResource(R.drawable.ic_audio);
            }
        }

        //Stores details of caller
        TextView textUserName = findViewById(R.id.nameText);
        TextView textEmail = findViewById(R.id.emailText);
        //Gets first name of caller
        String firstName = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        //Gets last name of caller as well
        textUserName.setText(String.format(
                "%s %s",
                firstName,
                getIntent().getStringExtra(Constants.KEY_LAST_NAME)
        ));
        //Gets caller email
        textEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        //Stores state of accept invitation button
        ImageView imageAcceptInvitation = findViewById(R.id.acceptCallIcon);

        //Waits for change in state of accept call button
        //If call is accepted, then constants changed to reflect this
        imageAcceptInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitationResponse(
                        Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                        getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
            }
        });

        //Stores state of reject invitation button
        ImageView imageRejectedInvitation = findViewById(R.id.rejectCallIcon);

        //Waits for change in state of reject call button
        //If call is rejected, then constants changed to reflect this
        imageRejectedInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitationResponse(
                        Constants.REMOTE_MSG_INVITATION_REJECTED,
                        getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
            }
        });
    }

    //Function to send back user response based on whether they accepted or rejected the call invitation
    protected void sendInvitationResponse(String type, String receiverToken) {
        try {

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            //Calls sendRemoteMessage function to respond to caller with user response
            sendRemoteMessage(body.toString(), type);

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Function to send user response to caller
    private void sendRemoteMessage(String remoteMessageBody, String type) {
        //Sends header and body to API which then connects user to same meeting room as caller
        ApiClient.getRetrofit().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                //Checks that both users are connected
                if (response.isSuccessful()) {
                    //If user accepts call invitation, then API called, connected both users to the same call
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                        try {
                            URL serverURL = new URL("https://meet.jit.si");

                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));
                            //Checks meeting type and sets room properties as necessary
                            if (meetingType.equals("audio")){
                                builder.setVideoMuted(true);
                            }
                            JitsiMeetActivity.launch(IncomingCall.this, builder.build());
                            finish();
                        } catch (Exception exception) {
                            Toast.makeText(IncomingCall.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } //If user rejects call invitation, then call ends and API not called
                     else {
                        Toast.makeText(IncomingCall.this, "Call Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else {
                    Toast.makeText(IncomingCall.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(IncomingCall.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    //Function to check caller call state
    //If call is cancelled caller side then call cancels
    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)) {
                    Toast.makeText(context, "Call Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    //Checks if user is being called when call is started
    //Creates a link between users with receiver for API
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
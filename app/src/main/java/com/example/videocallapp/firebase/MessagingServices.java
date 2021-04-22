package com.example.videocallapp.firebase;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.videocallapp.activities.IncomingCall;
import com.example.videocallapp.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

//Class to store all information required by API
//Data about call store here to be called by call activities
public class MessagingServices extends FirebaseMessagingService {

    //Creates new token for each instance of call
    //Creates token for each user
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //Stores call type
        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);

        //If call type is not null then call is taking place
        //All data needed for start call set here to be called when needed
        if (type != null){
            if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                Intent intent = new Intent(getApplicationContext(), IncomingCall.class);
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );
                intent.putExtra(
                        Constants.KEY_FIRST_NAME,
                        remoteMessage.getData().get(Constants.KEY_FIRST_NAME)
                );
                intent.putExtra(
                        Constants.KEY_LAST_NAME,
                        remoteMessage.getData().get(Constants.KEY_LAST_NAME)
                );
                intent.putExtra(
                        Constants.KEY_EMAIL,
                        remoteMessage.getData().get(Constants.KEY_EMAIL)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } //Stores response from call, sends this data to outgoing call activity to get API to start call or to cancel the call
              else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                Intent intent = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }

    }
}

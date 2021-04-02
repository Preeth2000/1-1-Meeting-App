package com.example.videocallapp.activities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videocallapp.R;
import com.example.videocallapp.models.User;
import com.example.videocallapp.network.APIClient;
import com.example.videocallapp.network.APIService;
import com.example.videocallapp.utilities.Constants;
import com.example.videocallapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Send_Invite extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private String invitertoken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send__invite);

        preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful() && task.getResult() != null) {
                    invitertoken = task.getResult().getToken();
                }
            }
        });

        ImageView imageMeetingType = findViewById(R.id.calltype);
        String meetingType = getIntent().getStringExtra("type");

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageMeetingType.setImageResource(R.drawable.ic_videoicon);
            } else {
                imageMeetingType.setImageResource(R.drawable.ic_audioicon);
            }
        }

        TextView firstchartext  = findViewById(R.id.firstchartext);
        TextView nametext   = findViewById(R.id.nametext);
        TextView emailtext      = findViewById(R.id.emailtext);

        User user = (User) getIntent().getSerializableExtra("user");
        if(user != null) {
            firstchartext.setText(user.firstname.substring(0,1));
            nametext.setText(String.format("%s %s", user.firstname, user.lastname));
            emailtext.setText(user.email);
        }

        ImageView cancelcall = findViewById(R.id.cancelcall);
        cancelcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(meetingType != null && user != null){
            startcall(meetingType, user.token);
        }

    }

    private void startcall(String meetingType, String recieverToken) {
        try {
            //Passing data from API
            JSONArray tokens = new JSONArray();
            tokens.put(recieverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, invitertoken);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        }catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        APIClient.getClient().create(APIService.class).sendremotemessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_MSG_INVITATION)){
                        Toast.makeText(Send_Invite.this, "Invitation sent", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Send_Invite.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(Send_Invite.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
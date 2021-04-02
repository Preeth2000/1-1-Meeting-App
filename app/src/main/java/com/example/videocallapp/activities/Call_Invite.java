package com.example.videocallapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.videocallapp.R;
import com.example.videocallapp.utilities.Constants;

public class Call_Invite extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call__invite);

        ImageView imageMeetingType = findViewById(R.id.calltype);
        String meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageMeetingType.setImageResource(R.drawable.ic_videoicon);
            } else {
                imageMeetingType.setImageResource(R.drawable.ic_audioicon);
            }
        }

        TextView firstchartext = findViewById(R.id.firstchartext);
        TextView nametext = findViewById(R.id.nametext);
        TextView emailtext = findViewById(R.id.emailtext);

        String firstName = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if (firstName != null) {
            firstchartext.setText(firstName.substring(0, 1));
        }

        nametext.setText(String.format("%s %s", firstName, getIntent().getStringExtra(Constants.KEY_LAST_NAME)));
        emailtext.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

    }


}
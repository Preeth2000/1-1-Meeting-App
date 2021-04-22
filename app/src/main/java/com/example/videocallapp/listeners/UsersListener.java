package com.example.videocallapp.listeners;

import com.example.videocallapp.models.User;

//Function that stores and returns the call function we want based on the call type, video or audio
public interface UsersListener {
    void initiateVideoCall(User user);

    void initiateAudioCall(User user);

}

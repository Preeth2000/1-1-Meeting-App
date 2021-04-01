package com.example.videocallapp.listener;

import com.example.videocallapp.models.User;

public interface UserListener {

    void initiatevideocall(User user);

    void initiateaudiocall(User user);

}

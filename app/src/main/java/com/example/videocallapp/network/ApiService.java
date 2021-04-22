package com.example.videocallapp.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

//Sends data to API in the structure we want
public interface ApiService {

    @POST("send")
    Call<String> sendRemoteMessage(
            @HeaderMap HashMap<String, String> headers,
            @Body String remoteBody
    );
}

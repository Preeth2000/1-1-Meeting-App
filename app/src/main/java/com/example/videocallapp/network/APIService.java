package com.example.videocallapp.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface APIService {

    @POST("send")
    Call<String> sendremotemessage(
            @HeaderMap HashMap<String, String> headers,
            @Body String remotebody
        );

}

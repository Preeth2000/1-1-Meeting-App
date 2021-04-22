package com.example.videocallapp.models;

import java.io.Serializable;

//Saves current user details to be converted to byte stream to be saved to disk
public class User implements Serializable {
    public String firstName, lastName, email, token;
}

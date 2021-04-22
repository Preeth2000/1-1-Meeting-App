package com.example.videocallapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

//Shared preferences
//Allows us to store data as key-value pairs
//Data stored here will persist even after the application is closed
//Allows us to store data that will persist when not using the application
public class PreferenceManager {

    private SharedPreferences sharedPreferences;

    public PreferenceManager(Context context){
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, context.MODE_PRIVATE);

    }
    //Allows us to create key value pairs that include booleans (True or False)
    //An example would be setting the user to signed in as true when user is signed in
    public void putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }

    //Allows us to create key value pairs that include strings
    //An example would be the users name or email that can be stored after they sign in
    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    //Clears all data stored in shared preferences
    //Used when user is signed out
    public void clearPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}

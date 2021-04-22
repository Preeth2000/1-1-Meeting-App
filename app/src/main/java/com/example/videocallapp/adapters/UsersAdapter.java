package com.example.videocallapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videocallapp.R;
import com.example.videocallapp.listeners.UsersListener;
import com.example.videocallapp.models.User;

import java.util.List;

//Class created to set user data onto our main activity class layout
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    //Created to store all users from database
    private List<User> users;
    //Created to store state of listener to listen for when a user presses either call button
    private UsersListener usersListener;

    public UsersAdapter(List<User> users, UsersListener usersListener) {
        this.users = users;
        this.usersListener = usersListener;
    }

    //Function that sets the container defined in item_container_user layout for use in Main activity layout
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_user,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    //Retrieves the number of users in database
    @Override
    public int getItemCount() {
        return users.size();
    }

    //Sub class to hold data to be put in user container layout
    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView  textUserName, textEmail;
        ImageView imageAudioCall, imageVideoCall;

        //Stores all data in each field on user container layout
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.nameText);
            textEmail = itemView.findViewById(R.id.emailText);
            imageAudioCall = itemView.findViewById(R.id.audioCallButton);
            imageVideoCall = itemView.findViewById(R.id.videoCallButton);
        }

        //Populates data on user container layout
        void setUserData(User user) {
            textUserName.setText(String.format("%s %s", user.firstName, user.lastName));
            textEmail.setText(user.email);
            imageAudioCall.setOnClickListener(view -> usersListener.initiateAudioCall(user));
            imageVideoCall.setOnClickListener(view -> usersListener.initiateVideoCall(user));
        }
    }
}
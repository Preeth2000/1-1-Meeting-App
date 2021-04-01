package com.example.videocallapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videocallapp.R;
import com.example.videocallapp.listener.UserListener;
import com.example.videocallapp.models.User;

import java.util.List;

public class UserAdapter extends  RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private UserListener userListener;

    public UserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.useritemcontainer,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        TextView firstchar, displayname, displayemail;
        ImageView videoimage, audioimage;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            firstchar = itemView.findViewById(R.id.firstchar);
            displayname = itemView.findViewById(R.id.displayname);
            displayemail = itemView.findViewById(R.id.displayemail);
            videoimage = itemView.findViewById(R.id.videoimage);
            audioimage = itemView.findViewById(R.id.audioimage);

        }

        void setUserData(User user) {
            firstchar.setText(user.firstname.substring(0, 1));
            displayname.setText(String.format("%s %s", user.firstname, user.lastname));
            displayemail.setText(user.email);
            audioimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userListener.initiateaudiocall(user);
                }
            });
            videoimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userListener.initiatevideocall(user);
                }
            });

        }
    }
}

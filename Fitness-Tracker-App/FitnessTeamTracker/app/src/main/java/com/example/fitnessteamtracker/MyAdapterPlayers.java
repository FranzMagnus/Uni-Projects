package com.example.fitnessteamtracker;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class MyAdapterPlayers extends RecyclerView.Adapter<MyAdapterPlayers.ViewHolder> {
    private String[] names;
    private int[] teams;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName, txtTeam;
        public Button btnFriend;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            txtName = (TextView) v.findViewById(R.id.txt_end_player);
            txtTeam = (TextView) v.findViewById(R.id.txt_end_teamID);
            btnFriend = v.findViewById(R.id.btn_end_friend);
        }
    }

    public MyAdapterPlayers(String[] names, int[] teams) {
        this.names = names;
        this.teams = teams;
    }

    @Override
    public MyAdapterPlayers.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.row_layout_players, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String name = names[position];
        int team = teams[position];
        holder.txtTeam.setText("Team " + team);
        holder.txtName.setText(name);

        holder.btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: send friend request
                Log.d("Friend", name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

}
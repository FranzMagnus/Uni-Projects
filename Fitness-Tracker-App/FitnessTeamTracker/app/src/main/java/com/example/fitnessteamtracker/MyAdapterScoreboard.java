package com.example.fitnessteamtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MyAdapterScoreboard extends RecyclerView.Adapter<MyAdapterScoreboard.ViewHolder> {
    private int[] teams;
    private int[] turnsPerTeam;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTeam;
        public TextView txtRound;
        public TextView txtPlace;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            txtTeam = (TextView) v.findViewById(R.id.txt_end_team);
            txtRound = (TextView) v.findViewById(R.id.txt_end_round);
            txtPlace = (TextView) v.findViewById(R.id.txt_end_place);
        }
    }

    public MyAdapterScoreboard(int[] teams, int[] turnsPerTeam) {
        this.teams = teams;
        this.turnsPerTeam = turnsPerTeam;
    }

    @Override
    public MyAdapterScoreboard.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.row_layout_scoreboard, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (position == 0) {
            holder.txtTeam.setText("Team");
            holder.txtPlace.setText("Place");
            holder.txtRound.setText("Turns");
            return;
        }
        int pos = position - 1;

        final String name = "Team " + teams[pos];
        int round = turnsPerTeam[teams[pos]];
        final String r = Integer.toString(round);
        final String pl = (pos + 1) + ".";
        holder.txtTeam.setText(name);

        holder.txtRound.setText(r);
        holder.txtPlace.setText(pl);
    }

    @Override
    public int getItemCount() {
        return teams.length + 1;
    }

}
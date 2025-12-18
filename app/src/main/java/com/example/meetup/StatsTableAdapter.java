package com.example.meetup;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StatsTableAdapter extends RecyclerView.Adapter<StatsTableAdapter.StatViewHolder> {

    private final List<StatRow> items = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<StatRow> newRows) {
        items.clear();
        items.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stat_row, parent, false);
        return new StatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
        StatRow row = items.get(position);
        holder.name.setText(row.getName());
        holder.phone.setText(row.getPhone());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StatViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone, status;

        public StatViewHolder(@NonNull View itemView) {
            super(itemView);
            name   = itemView.findViewById(R.id.colName);
            phone  = itemView.findViewById(R.id.colPhone);
        }
    }
}

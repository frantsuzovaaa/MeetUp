package com.example.meetup.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;
import com.example.meetup.events.Events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterEvents extends RecyclerView.Adapter<AdapterEvents.MyViewHolder> {
    Context context;
    ArrayList<Events> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Events event, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public AdapterEvents(Context context, ArrayList<Events> list) {
        this.context = context;
        this.list = list;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateEvents(ArrayList<Events> newEvents) {
        this.list.clear();
        this.list.addAll(newEvents);
        notifyDataSetChanged();
        Log.d("DEBUG", "Adapter updated with " + newEvents.size() + " events");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.event, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Events events = list.get(position);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        holder.nameEvent.setText(events.getNameEvent());
        holder.date.setText(simpleDateFormat.format(new Date(events.getDataTime())));
        Log.d("DEBUG", "Binding event: " + events.getNameEvent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nameEvent, date;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameEvent = itemView.findViewById(R.id.nameEventText);
            date = itemView.findViewById(R.id.timeText);
        }
    }
}
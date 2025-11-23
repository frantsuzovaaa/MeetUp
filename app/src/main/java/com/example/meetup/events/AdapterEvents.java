package com.example.meetup.events;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterEvents extends RecyclerView.Adapter<AdapterEvents.MyViewHolder> {
    Context context;
    ArrayList<Events> list;
    ArrayList<String> eventId_list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Events event, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public AdapterEvents(Context context, ArrayList<Events> list, ArrayList<String> list_id) {
        this.context = context;
        this.list = list;
        this.eventId_list= list_id;
    }

    public String getEventId(int position){
        if (eventId_list!= null && position < eventId_list.size()){
            return eventId_list.get(position);
        }
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateEvents(ArrayList<Events> newEvents, ArrayList<String> ids) {
        this.list.clear();
        this.list.addAll(newEvents);
        this.eventId_list.clear();
        this.eventId_list.addAll(ids);
        notifyDataSetChanged();
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
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAbsoluteAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Events currentEvent = list.get(currentPosition);
                listener.onItemClick(currentEvent, currentPosition);
            }
        });


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
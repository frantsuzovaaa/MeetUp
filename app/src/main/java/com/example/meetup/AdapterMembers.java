package com.example.meetup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterMembers extends RecyclerView.Adapter<AdapterMembers.MyViewHolder> {
    ArrayList<Member> list;
    ArrayList<String> list_id_member;
    Context context;
    private OnItemClickListener listener;
    private  OnChangesClickListener changesClickListener;
    private  OnOpenQrClickListener onOpenQrClickListener;

    public interface OnItemClickListener {
        void onItemClick(Member member, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public interface OnChangesClickListener {
        void onChangesClick(Member member, int position);
    }

    public void setOnChangesClickListener(OnChangesClickListener listener) {
        this.changesClickListener = listener;
    }

    public interface OnOpenQrClickListener {
    }

    public void setOnOpenQrClickListener(OnOpenQrClickListener listener) {
        this.onOpenQrClickListener = listener;
    }

    public AdapterMembers(Context context, ArrayList<Member> list, ArrayList<String> list_id_member) {
        this.context = context;
        this.list = list;
        this.list_id_member = list_id_member;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMembers(ArrayList<Member> newMembers, ArrayList<String> newIds) {
        this.list.clear();
        this.list.addAll(newMembers);
        this.list_id_member.clear();
        this.list_id_member.addAll(newIds);
        notifyDataSetChanged();
    }

    public String getMemberId(int position){
        if (list_id_member!=null && position < list_id_member.size()){
            return list_id_member.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.members, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Member member = list.get(position);

        holder.name.setText(member.getName());
        holder.number.setText(member.getNumber());

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAbsoluteAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(member, currentPosition);
            }
        });

        holder.bntChanges.setOnClickListener(v -> {
            int currentPosition = holder.getAbsoluteAdapterPosition();
                changesClickListener.onChangesClick(member, currentPosition);
            }
        });

        holder.btnQR.setOnClickListener(v -> {
            int currentPosition = holder.getAbsoluteAdapterPosition();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, number;
        AppCompatImageButton bntChanges, btnQR;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameMember);
            number = itemView.findViewById(R.id.number);

            bntChanges = itemView.findViewById(R.id.buttonEdit);
            btnQR = itemView.findViewById(R.id.qr_sending);

        }
    }
}
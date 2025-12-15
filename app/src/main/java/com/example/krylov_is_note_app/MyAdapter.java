package com.example.krylov_is_note_app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private final String TAG = "MyAdapter";
    private List<CardData> dataSource;
    private Activity activity;
    private OnItemClickListener itemClickListener;
    private int menuPosition;

    public int getMenuPosition() {
        return menuPosition;
    }

    public MyAdapter(List<CardData> dataSource, Activity activity) {
        this.dataSource = dataSource != null ? dataSource : new ArrayList<>();
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        Log.d(TAG, "onCreateViewHolder");
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(dataSource.get(position));
        Log.d(TAG, "onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<CardData> newData) {
        this.dataSource.clear();
        if (newData != null) {
            this.dataSource.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void SetOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);

            if (activity != null) {
                activity.registerForContextMenu(itemView);
            }

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, getAdapterPosition());
                }
            });


            itemView.setOnLongClickListener(v -> {
                menuPosition = getAdapterPosition();
                v.showContextMenu();
                return true;
            });
        }

        public void setData(CardData cardData) {
            title.setText(cardData.getTitle());
            description.setText(cardData.getDescription());
        }
    }
}
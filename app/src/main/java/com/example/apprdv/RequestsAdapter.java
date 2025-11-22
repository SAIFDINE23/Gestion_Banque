package com.example.apprdv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private List<HashMap<String, String>> requests;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HashMap<String, String> request);
    }

    public RequestsAdapter(List<HashMap<String, String>> requests, OnItemClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        HashMap<String, String> request = requests.get(position);
        holder.tvClientName.setText(request.get("clientName"));
        holder.tvReason.setText(request.get("reason"));
        holder.tvDesiredDate.setText(request.get("desiredDate"));
        holder.tvAgency.setText(request.get("agency"));
        holder.tvStatus.setText(request.get("status"));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(request));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvReason, tvDesiredDate, tvAgency, tvStatus;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvDesiredDate = itemView.findViewById(R.id.tvDesiredDate);
            tvAgency = itemView.findViewById(R.id.tvAgency);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}

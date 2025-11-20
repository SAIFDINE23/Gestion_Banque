package com.example.apprdv;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Map<String, Object>> clientList;
    private Context context;

    public ClientAdapter(Context context, List<Map<String, Object>> clientList) {
        this.context = context;
        this.clientList = clientList;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Map<String, Object> client = clientList.get(position);

        String name = client.get("name") != null ? String.valueOf(client.get("name")) : "Nom inconnu";
        String agency = client.get("agency") != null ? String.valueOf(client.get("agency")) : "Agence inconnue";

        // Lire date/slot (clés depuis appointments)
        String date = client.get("selectedDate") != null ? String.valueOf(client.get("selectedDate")) : "--/--";
        String slot = client.get("selectedSlot") != null ? String.valueOf(client.get("selectedSlot")) : "--:--";

        holder.tvName.setText(name);
        holder.tvAgency.setText(agency);
        holder.tvDate.setText(date);
        holder.tvTime.setText(slot);

        holder.itemView.setOnClickListener(v -> {
            String clientId = client.get("id") != null ? String.valueOf(client.get("id")) : null;
            if (clientId != null) {
                Intent intent = new Intent(context, ClientDetailsActivity.class);
                intent.putExtra("CLIENT_ID", clientId);
                context.startActivity(intent);
            } else {
                // Optional: toast ou log si id manquant
            }
        });
    }


    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAgency, tvDate, tvTime;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvClientName);
            tvAgency = itemView.findViewById(R.id.tvClientAgency);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

}

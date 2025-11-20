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
        holder.tvName.setText((String) client.get("name"));
        holder.tvAgency.setText((String) client.get("agency"));

        // 🔹 Ajouter un clic pour chaque item
        holder.itemView.setOnClickListener(v -> {
            String clientId = (String) client.get("id"); // récupère l'id du client
            Intent intent = new Intent(context, ClientDetailsActivity.class);
            intent.putExtra("CLIENT_ID", clientId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAgency;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvClientName);
            tvAgency = itemView.findViewById(R.id.tvClientAgency);
        }
    }
}

package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdvisorHomeActivity extends AppCompatActivity {

    private TextView tvAdvisorName;
    private RecyclerView rvClients;
    private ClientAdapter clientAdapter;
    private List<Map<String, Object>> clientList = new ArrayList<>();

    private String currentAdvisorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advisor_home);

        tvAdvisorName = findViewById(R.id.tvAdvisorName);
        rvClients = findViewById(R.id.rvClients);
        rvClients.setLayoutManager(new LinearLayoutManager(this));
        clientAdapter = new ClientAdapter(this, clientList);
        rvClients.setAdapter(clientAdapter);

        Button btnAllClients = findViewById(R.id.btnAllClients);
        btnAllClients.setOnClickListener(v -> {
            Intent intent = new Intent(AdvisorHomeActivity.this, AllClientsActivity.class);
            startActivity(intent);
        });




        // ⚡ Récupérer l'ID de l'advisor depuis l'intent
        currentAdvisorId = getIntent().getStringExtra("advisorId");
        if (currentAdvisorId == null) {
            Toast.makeText(this, "Erreur : conseiller introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button btnChangeRequests = findViewById(R.id.btnChangeRequests);
        btnChangeRequests.setOnClickListener(v -> {
            Intent intent = new Intent(AdvisorHomeActivity.this, AdvisorRequestsActivity.class);
            intent.putExtra("advisorId", currentAdvisorId);
            startActivity(intent);
        });

        loadAdvisorName();
        loadClients();
    }

    private void loadAdvisorName() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("advisors").child(currentAdvisorId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    tvAdvisorName.setText("Bienvenue, " + name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdvisorHomeActivity.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClients() {
        DatabaseReference clientsRef = FirebaseDatabase.getInstance().getReference("clients");
        DatabaseReference rdvRef = FirebaseDatabase.getInstance().getReference("appointments");

        clientsRef.orderByChild("myAdvisor").equalTo(currentAdvisorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        clientList.clear();

                        if (!snapshot.exists()) {
                            clientAdapter.notifyDataSetChanged();
                            return;
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Object value = ds.getValue();
                            if (!(value instanceof Map)) continue;
                            @SuppressWarnings("unchecked")
                            Map<String, Object> client = (Map<String, Object>) value;

                            client.put("selectedDate", "--/--");
                            client.put("selectedSlot", "--:--");

                            clientList.add(client);
                            clientAdapter.notifyItemInserted(clientList.size() - 1);

                            String appointmentId = client.get("appointmentId") != null ?
                                    String.valueOf(client.get("appointmentId")) : null;

                            if (appointmentId != null && !appointmentId.isEmpty()) {
                                final int index = clientList.indexOf(client);
                                rdvRef.child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot rdvSnap) {
                                        if (rdvSnap.exists()) {
                                            String date = rdvSnap.child("selectedDate").getValue(String.class);
                                            String slot = rdvSnap.child("selectedSlot").getValue(String.class);

                                            client.put("selectedDate", date != null ? date : "--/--");
                                            client.put("selectedSlot", slot != null ? slot : "--:--");

                                            if (index >= 0 && index < clientList.size()) {
                                                clientAdapter.notifyItemChanged(index);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}

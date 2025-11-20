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

    // Mettre ici l'ID de l'advisor connecté (récupéré depuis login)
    private String currentAdvisorId = "-OZpTWIz-hSoSGbH_tBp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advisor_home);

        tvAdvisorName = findViewById(R.id.tvAdvisorName);
        rvClients = findViewById(R.id.rvClients);
        rvClients.setLayoutManager(new LinearLayoutManager(this));
        clientAdapter = new ClientAdapter(this,clientList);
        rvClients.setAdapter(clientAdapter);
        Button btnAllClients = findViewById(R.id.btnAllClients);
        btnAllClients.setOnClickListener(v -> {
            Intent intent = new Intent(AdvisorHomeActivity.this, AllClientsActivity.class);
            startActivity(intent);
        });


        loadClients();
        loadAdvisorName();
    }

    private void loadAdvisorName() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("advisors").child(currentAdvisorId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("clients");
        DatabaseReference rdvRef = FirebaseDatabase.getInstance().getReference("appointments");

        ref.orderByChild("myAdvisor").equalTo(currentAdvisorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        clientList.clear();

                        if (!snapshot.exists()) {
                            clientAdapter.notifyDataSetChanged();
                            return;
                        }

                        // Pour chaque client récupéré
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Sécuriser le cast: si pb -> continuer
                            Object value = ds.getValue();
                            if (!(value instanceof Map)) {
                                continue;
                            }
                            @SuppressWarnings("unchecked")
                            Map<String, Object> client = (Map<String, Object>) value;

                            // Valeurs par défaut pour l'affichage du rdv
                            client.put("selectedDate", "--/--");
                            client.put("selectedSlot", "--:--");

                            // Ajouter tout de suite le client (permet d'afficher quelque chose vite)
                            clientList.add(client);
                            clientAdapter.notifyItemInserted(clientList.size() - 1);

                            // Récupérer l'appointmentId (peut être null)
                            String appointmentId = null;
                            if (client.get("appointmentId") != null) {
                                appointmentId = String.valueOf(client.get("appointmentId"));
                            } else if (client.get("appointmentID") != null) { // fallback si typo
                                appointmentId = String.valueOf(client.get("appointmentID"));
                            }

                            if (appointmentId != null && !appointmentId.isEmpty()) {
                                // Charger le rdv et mettre à jour l'objet client dans la liste
                                final int index = clientList.indexOf(client); // position du client ajouté
                                rdvRef.child(appointmentId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot rdvSnap) {
                                                if (rdvSnap.exists()) {
                                                    String date = rdvSnap.child("selectedDate").getValue(String.class);
                                                    String slot = rdvSnap.child("selectedSlot").getValue(String.class);

                                                    // Mettre à jour la map client (gardez les clés cohérentes)
                                                    client.put("selectedDate", date != null ? date : "--/--");
                                                    client.put("selectedSlot", slot != null ? slot : "--:--");

                                                    // Notify adapter pour cet item (meilleur perf que notifyDataSetChanged)
                                                    if (index >= 0 && index < clientList.size()) {
                                                        clientAdapter.notifyItemChanged(index);
                                                    } else {
                                                        clientAdapter.notifyDataSetChanged();
                                                    }
                                                } else {
                                                    // rdv non trouvé -> laisser les valeurs par défaut
                                                    if (index >= 0 && index < clientList.size()) {
                                                        clientAdapter.notifyItemChanged(index);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                // en cas d'erreur réseau: on peut logguer et garder valeurs par défaut
                                                if (index >= 0 && index < clientList.size()) {
                                                    clientAdapter.notifyItemChanged(index);
                                                }
                                            }
                                        });
                            } // sinon on garde les valeurs par défaut
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // gérer erreur globale (ex: afficher Toast)
                    }
                });
    }


}

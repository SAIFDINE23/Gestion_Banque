package com.example.apprdv;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestDetailActivity extends AppCompatActivity {

    TextView tvClientName, tvReason, tvDesiredDate, tvAgency, tvStatus;
    EditText etAdvisorJustification;
    Button btnAccept, btnRefuse;
    String requestId;
    DatabaseReference requestRef, clientsRef, advisorsRef;

    String clientId; // stocke le clientId associé à la request

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        tvClientName = findViewById(R.id.tvClientName);
        tvReason = findViewById(R.id.tvReason);
        tvDesiredDate = findViewById(R.id.tvDesiredDate);
        tvAgency = findViewById(R.id.tvAgency);
        tvStatus = findViewById(R.id.tvStatus);
        etAdvisorJustification = findViewById(R.id.etAdvisorJustification);
        btnAccept = findViewById(R.id.btnAccept);
        btnRefuse = findViewById(R.id.btnRefuse);

        etAdvisorJustification.setVisibility(EditText.GONE); // caché par défaut

        requestId = getIntent().getStringExtra("requestId");
        if (requestId == null || requestId.isEmpty()) {
            Toast.makeText(this, "Aucune demande fournie", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        requestRef = FirebaseDatabase.getInstance().getReference("changeAdvisorRequests").child(requestId);
        clientsRef = FirebaseDatabase.getInstance().getReference("clients");
        advisorsRef = FirebaseDatabase.getInstance().getReference("advisors");

        loadRequestDetails();

        btnAccept.setOnClickListener(v -> acceptRequest());
        btnRefuse.setOnClickListener(v -> refuseRequest());
    }

    private void loadRequestDetails() {
        requestRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                tvClientName.setText(snapshot.child("clientName").getValue(String.class));
                tvReason.setText(snapshot.child("reason").getValue(String.class));
                tvDesiredDate.setText(snapshot.child("desiredDate").getValue(String.class));
                tvAgency.setText(snapshot.child("agency").getValue(String.class));
                tvStatus.setText(snapshot.child("status").getValue(String.class));

                clientId = snapshot.child("clientId").getValue(String.class);

                // Si la demande a déjà été refusée et contient une justification
                String justify = snapshot.child("advisorJustification").getValue(String.class);
                if (justify != null && !justify.isEmpty()) {
                    etAdvisorJustification.setText(justify);
                    etAdvisorJustification.setVisibility(EditText.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RequestDetailActivity.this, "Erreur DB: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refuseRequest() {
        String justification = etAdvisorJustification.getText().toString().trim();
        if (justification.isEmpty()) {
            etAdvisorJustification.setError("Veuillez saisir une justification");
            etAdvisorJustification.setVisibility(EditText.VISIBLE);
            return;
        }
        requestRef.child("status").setValue("refused");
        requestRef.child("advisorJustification").setValue(justification);
        Toast.makeText(this, "Demande refusée", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void acceptRequest() {
        if (clientId == null || clientId.isEmpty()) {
            Toast.makeText(this, "Client non trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lire le client actuel pour obtenir son myAdvisor
        clientsRef.child(clientId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot clientSnapshot) {
                if (!clientSnapshot.exists()) return;

                String currentAdvisor = clientSnapshot.child("myAdvisor").getValue(String.class);

                // Récupérer tous les advisors
                advisorsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot advisorsSnapshot) {
                        if (!advisorsSnapshot.exists()) return;

                        ArrayList<String> advisorIds = new ArrayList<>();
                        for (DataSnapshot ds : advisorsSnapshot.getChildren()) {
                            String id = ds.getKey();
                            if (id != null && !id.equals(currentAdvisor)) {
                                advisorIds.add(id);
                            }
                        }

                        if (advisorIds.isEmpty()) {
                            Toast.makeText(RequestDetailActivity.this, "Aucun advisor disponible", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Compter le nombre de clients pour chaque advisor
                        Map<String, Integer> advisorClientCount = new HashMap<>();
                        clientsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot clientsSnapshot) {
                                for (DataSnapshot c : clientsSnapshot.getChildren()) {
                                    String adv = c.child("myAdvisor").getValue(String.class);
                                    if (adv != null) {
                                        advisorClientCount.put(adv, advisorClientCount.getOrDefault(adv, 0) + 1);
                                    }
                                }

                                // Mélanger la liste et choisir le premier advisor avec moins de 10 clients
                                Collections.shuffle(advisorIds);
                                String newAdvisor = null;
                                for (String advId : advisorIds) {
                                    int count = advisorClientCount.getOrDefault(advId, 0);
                                    if (count < 10) {
                                        newAdvisor = advId;
                                        break;
                                    }
                                }

                                if (newAdvisor == null) {
                                    Toast.makeText(RequestDetailActivity.this, "Aucun advisor disponible avec moins de 10 clients", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Mettre à jour le client et la demande
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("myAdvisor", newAdvisor);
                                clientsRef.child(clientId).updateChildren(updates);

                                requestRef.child("status").setValue("accepted");
                                requestRef.child("advisorJustification").setValue("");

                                Toast.makeText(RequestDetailActivity.this, "Demande acceptée et client réaffecté", Toast.LENGTH_LONG).show();
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(RequestDetailActivity.this, "Erreur DB: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RequestDetailActivity.this, "Erreur DB: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RequestDetailActivity.this, "Erreur DB: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

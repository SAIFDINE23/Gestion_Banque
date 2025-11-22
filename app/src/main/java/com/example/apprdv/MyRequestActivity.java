package com.example.apprdv;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyRequestActivity extends AppCompatActivity {

    TextView tvRequestId, tvStatus, tvReason, tvDesiredDate, tvSendingDate, tvAdvisorJustify, tvAgency;
    DatabaseReference requestsRef;
    String clientNodeKey; // clé Firebase du client

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_request);

        // récupérer clientId (nodeKey) passé via Intent
        clientNodeKey = getIntent().getStringExtra("clientId");
        if (clientNodeKey == null || clientNodeKey.isEmpty()) {
            Toast.makeText(this, "Aucune information client fournie.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        requestsRef = FirebaseDatabase.getInstance().getReference("changeAdvisorRequests");

        initViews();
        loadLatestRequestForClient();
    }

    private void initViews() {
        tvRequestId = findViewById(R.id.tvRequestId);
        tvStatus = findViewById(R.id.tvStatus);
        tvReason = findViewById(R.id.tvReason);
        tvDesiredDate = findViewById(R.id.tvDesiredDate);
        tvSendingDate = findViewById(R.id.tvSendingDate);
        tvAdvisorJustify = findViewById(R.id.tvAdvisorJustify);
        tvAgency = findViewById(R.id.tvAgency);
    }

    private void loadLatestRequestForClient() {
        // Récupérer toutes les demandes du client
        requestsRef.orderByChild("clientId").equalTo(clientNodeKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(MyRequestActivity.this, "Aucune demande trouvée.", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        // Trouver la demande la plus récente
                        DataSnapshot latest = null;
                        long maxTimestamp = Long.MIN_VALUE;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Long ts = ds.child("sendingDate").getValue(Long.class);
                            if (ts == null) ts = 0L;
                            if (ts >= maxTimestamp) {
                                maxTimestamp = ts;
                                latest = ds;
                            }
                        }

                        if (latest == null) {
                            Toast.makeText(MyRequestActivity.this, "Aucune demande valide trouvée.", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        // Récupérer les valeurs (incluant toAdvisor mais non affiché)
                        String requestId = latest.getKey();
                        String status = latest.child("status").getValue(String.class);
                        String reason = latest.child("reason").getValue(String.class);
                        String desiredDate = latest.child("desiredDate").getValue(String.class);
                        Long sendingTimestamp = latest.child("sendingDate").getValue(Long.class);
                        String justify = latest.child("advisorJustification").getValue(String.class);
                        String agency = latest.child("agency").getValue(String.class);
                        String toAdvisor = latest.child("toAdvisor").getValue(String.class); // nouveau champ, non affiché

                        // Mettre à jour l’UI côté client
                        tvRequestId.setText(requestId != null ? requestId : "—");
                        tvStatus.setText(status != null ? status : "—");
                        tvReason.setText(reason != null ? reason : "—");
                        tvDesiredDate.setText(desiredDate != null ? desiredDate : "—");
                        tvAgency.setText(agency != null ? agency : "—");

                        // Afficher la justification seulement si status = refused
                        if (status != null && status.equalsIgnoreCase("refused")) {
                            if (justify != null && !justify.isEmpty()) {
                                tvAdvisorJustify.setText(justify);
                            } else {
                                tvAdvisorJustify.setText("Aucune justification fournie");
                            }
                            tvAdvisorJustify.setVisibility(View.VISIBLE);
                        } else {
                            tvAdvisorJustify.setVisibility(View.GONE);
                        }

                        if (sendingTimestamp != null && sendingTimestamp > 0) {
                            tvSendingDate.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", sendingTimestamp));
                        } else {
                            tvSendingDate.setText("—");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MyRequestActivity.this, "Erreur DB: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }
}

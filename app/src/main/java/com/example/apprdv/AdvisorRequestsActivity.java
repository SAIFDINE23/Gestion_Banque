package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;

public class AdvisorRequestsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RequestsAdapter adapter;
    List<HashMap<String, String>> requestList = new ArrayList<>();
    DatabaseReference requestsRef;
    String advisorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advisor_requests);

        recyclerView = findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestsAdapter(requestList, map -> {
            // Clic sur une request → ouvrir détails
            Intent intent = new Intent(AdvisorRequestsActivity.this, RequestDetailActivity.class);
            intent.putExtra("requestId", map.get("requestId"));
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        advisorId = getIntent().getStringExtra("advisorId");
        if (advisorId == null || advisorId.isEmpty()) {
            Toast.makeText(this, "Aucun ID d'advisor fourni.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        requestsRef = FirebaseDatabase.getInstance().getReference("changeAdvisorRequests");
        loadPendingRequestsForAdvisor();
    }

    private void loadPendingRequestsForAdvisor() {
        requestsRef.orderByChild("toAdvisor").equalTo(advisorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        requestList.clear();
                        if (!snapshot.exists()) {
                            Toast.makeText(AdvisorRequestsActivity.this, "Aucune demande en attente.", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String status = ds.child("status").getValue(String.class);
                            if (status != null && status.equalsIgnoreCase("pending")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("requestId", ds.getKey());
                                map.put("clientName", ds.child("clientName").getValue(String.class));
                                map.put("reason", ds.child("reason").getValue(String.class));
                                map.put("desiredDate", ds.child("desiredDate").getValue(String.class));
                                map.put("agency", ds.child("agency").getValue(String.class));
                                map.put("status", status);

                                requestList.add(map);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdvisorRequestsActivity.this, "Erreur DB: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

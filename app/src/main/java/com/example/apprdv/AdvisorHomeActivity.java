package com.example.apprdv;

import android.os.Bundle;
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
        ref.orderByChild("myAdvisor").equalTo(currentAdvisorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        clientList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Map<String, Object> client = (Map<String, Object>) ds.getValue();
                            clientList.add(client);
                        }
                        clientAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdvisorHomeActivity.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.apprdv;

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
import java.util.List;
import java.util.Map;

public class AllClientsActivity extends AppCompatActivity {

    private RecyclerView rvAllClients;
    private ClientAdapter clientAdapter;
    private List<Map<String, Object>> clientList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_clients);

        rvAllClients = findViewById(R.id.rvAllClients);
        rvAllClients.setLayoutManager(new LinearLayoutManager(this));
        clientAdapter = new ClientAdapter(this, clientList);
        rvAllClients.setAdapter(clientAdapter);

        loadAllClients();
    }

    private void loadAllClients() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("clients");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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
                Toast.makeText(AllClientsActivity.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

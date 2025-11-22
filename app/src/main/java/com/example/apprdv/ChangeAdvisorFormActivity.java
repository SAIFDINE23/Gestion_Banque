package com.example.apprdv;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChangeAdvisorFormActivity extends AppCompatActivity {

    EditText etClientName, etClientId, etClientAgency, etClientAddress;
    EditText etReason, etDesiredDate;
    Button btnSubmitRequest;

    DatabaseReference clientsRef, requestsRef;
    String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_advisor_form);

        clientId = getIntent().getStringExtra("clientId");

        clientsRef = FirebaseDatabase.getInstance().getReference("clients");
        requestsRef = FirebaseDatabase.getInstance().getReference("changeAdvisorRequests");

        initViews();
        loadClientData();
        setupDatePicker();
    }

    private void initViews() {
        etClientName = findViewById(R.id.etClientName);
        etClientId = findViewById(R.id.etClientId);
        etClientAgency = findViewById(R.id.etClientAgency);
        etClientAddress = findViewById(R.id.etClientAddress);

        etReason = findViewById(R.id.etReason);
        etDesiredDate = findViewById(R.id.etDesiredDate);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        btnSubmitRequest.setOnClickListener(v -> submitRequest());
    }

    private void loadClientData() {
        clientsRef.child(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String name = snapshot.child("name").getValue(String.class);
                String surname = snapshot.child("surname").getValue(String.class);
                String agency = snapshot.child("agency").getValue(String.class);
                String address = snapshot.child("adresse").getValue(String.class);
                String id = snapshot.child("id").getValue(String.class);

                etClientName.setText(name + " " + surname);
                etClientId.setText(id);
                etClientAgency.setText(agency);
                etClientAddress.setText(address);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupDatePicker() {
        etDesiredDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                etDesiredDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void submitRequest() {

        String reason = etReason.getText().toString().trim();
        String desiredDate = etDesiredDate.getText().toString().trim();

        if (reason.isEmpty()) {
            Toast.makeText(this, "Veuillez expliquer la raison du changement", Toast.LENGTH_SHORT).show();
            return;
        }

        String requestId = requestsRef.push().getKey();

        // Récupérer l'id de l'advisor actuel avant de créer la demande
        clientsRef.child(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String currentAdvisorId = snapshot.child("myAdvisor").getValue(String.class);

                Map<String, Object> map = new HashMap<>();
                map.put("clientId", clientId);
                map.put("clientName", etClientName.getText().toString());
                map.put("agency", etClientAgency.getText().toString());
                map.put("accountId", etClientId.getText().toString());
                map.put("reason", reason);
                map.put("desiredDate", desiredDate);
                map.put("sendingDate", System.currentTimeMillis());
                map.put("status", "pending");
                map.put("advisorJustification", "");
                map.put("toAdvisor", currentAdvisorId != null ? currentAdvisorId : ""); // 🔹 id de l'advisor actuel

                // Ajouter la demande et mettre à jour le client avec myRequest
                requestsRef.child(requestId).setValue(map)
                        .addOnSuccessListener(aVoid -> {
                            clientsRef.child(clientId).child("myRequest").setValue(requestId);
                            Toast.makeText(ChangeAdvisorFormActivity.this, "Demande envoyée", Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(ChangeAdvisorFormActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


}

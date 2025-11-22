package com.example.apprdv;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClientHomeActivity extends AppCompatActivity {

    private LinearLayout advisorsContainer;
    private DatabaseReference mDatabase;
    private DatabaseReference clientsRef;

    private static final String[] DEFAULT_SLOTS = {"10:00", "11:00", "12:00", "14:00", "15:00", "16:00"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);

        advisorsContainer = findViewById(R.id.advisorsContainer);
        mDatabase = FirebaseDatabase.getInstance().getReference("advisors");
        clientsRef = FirebaseDatabase.getInstance().getReference("clients");

        String clientId = getIntent().getStringExtra("clientId");
        if (clientId == null) {
            Toast.makeText(this, "Erreur : client introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button btnChangeAdvisor = findViewById(R.id.btnChangeAdvisor);
        Button btnViewRequests = findViewById(R.id.btnViewRequests);

        // ⚡ Vérifier si le client a plus d'un an de création
        clientsRef.child(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String creationDateStr = snapshot.child("creationDate").getValue(String.class);
                String myAdvisor = snapshot.child("myAdvisor").getValue(String.class);
                String clientAgency = snapshot.child("agency").getValue(String.class);

                boolean showButtons = false;

                if (creationDateStr != null && !creationDateStr.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        Date creationDate = sdf.parse(creationDateStr);
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.YEAR, -1);
                        Date oneYearAgo = cal.getTime();

                        // Si la création date de plus d’un an
                        if (creationDate.before(oneYearAgo)) {
                            showButtons = true;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // Afficher ou cacher les boutons
                btnChangeAdvisor.setVisibility(showButtons ? Button.VISIBLE : Button.GONE);
                btnViewRequests.setVisibility(showButtons ? Button.VISIBLE : Button.GONE);

                // ⚡ Afficher advisors
                if (myAdvisor != null && !myAdvisor.isEmpty()) {
                    loadSingleAdvisor(myAdvisor);
                } else {
                    loadAllAdvisors(clientAgency);
                }

                // Click listeners pour les boutons
                btnChangeAdvisor.setOnClickListener(v -> {
                    Intent intent = new Intent(ClientHomeActivity.this, ChangeAdvisorFormActivity.class);
                    intent.putExtra("clientId", clientId);
                    startActivity(intent);
                });

                btnViewRequests.setOnClickListener(v -> {
                    Intent intent = new Intent(ClientHomeActivity.this, MyRequestActivity.class);
                    intent.putExtra("clientId", clientId);
                    startActivity(intent);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadSingleAdvisor(String advisorId) {
        mDatabase.child(advisorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                advisorsContainer.removeAllViews();
                if (data.exists()) {
                    Advisor advisor = data.getValue(Advisor.class);
                    if (advisor != null) {
                        advisor.setAdvisorId(data.getKey());
                        advisorsContainer.addView(createAdvisorButton(advisor));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadAllAdvisors(String clientAgency) {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                advisorsContainer.removeAllViews();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Advisor advisor = data.getValue(Advisor.class);
                    if (advisor != null) {
                        advisor.setAdvisorId(data.getKey());
                        if (advisor.getAgency() != null && advisor.getAgency().equals(clientAgency)) {
                            advisorsContainer.addView(createAdvisorButton(advisor));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private Button createAdvisorButton(Advisor advisor) {
        Button btn = new Button(ClientHomeActivity.this);
        btn.setText("Nom : " + advisor.getName() + "\nAdresse : " + advisor.getAddress() + "\nAgence : " + advisor.getAgency());

        btn.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ClientHomeActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar chosenDate = Calendar.getInstance();
                        chosenDate.set(selectedYear, selectedMonth, selectedDay);
                        if (chosenDate.before(Calendar.getInstance())) {
                            Toast.makeText(ClientHomeActivity.this, "Impossible de choisir une date passée", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String selectedDate = String.format("%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDay);

                        DatabaseReference availabilityRef = FirebaseDatabase.getInstance()
                                .getReference("advisors")
                                .child(advisor.getAdvisorId())
                                .child("availability")
                                .child(selectedDate);

                        availabilityRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    Map<String, Boolean> slots = new HashMap<>();
                                    for (String slot : DEFAULT_SLOTS) slots.put(slot, true);
                                    availabilityRef.setValue(slots)
                                            .addOnSuccessListener(aVoid -> goToSlotsPage(advisor, selectedDate))
                                            .addOnFailureListener(e -> Toast.makeText(ClientHomeActivity.this, "Erreur lors de l'ajout de la date", Toast.LENGTH_SHORT).show());
                                } else {
                                    goToSlotsPage(advisor, selectedDate);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }, year, month, day);
            datePickerDialog.show();
        });

        return btn;
    }

    private void goToSlotsPage(Advisor advisor, String selectedDate) {
        Intent intent = new Intent(ClientHomeActivity.this, ChooseSlotActivity.class);
        intent.putExtra("advisorName", advisor.getName());
        intent.putExtra("advisorId", advisor.getAdvisorId());
        intent.putExtra("selectedDate", selectedDate);
        startActivity(intent);
    }
}

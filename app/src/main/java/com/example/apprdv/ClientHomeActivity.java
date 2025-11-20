package com.example.apprdv;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ClientHomeActivity extends AppCompatActivity {

    private LinearLayout advisorsContainer;
    private DatabaseReference mDatabase;
    private DatabaseReference clientsRef;

    // ⚡ Les créneaux par défaut
    private static final String[] DEFAULT_SLOTS = {"10:00", "11:00", "12:00", "14:00", "15:00", "16:00"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);

        advisorsContainer = findViewById(R.id.advisorsContainer);
        mDatabase = FirebaseDatabase.getInstance().getReference("advisors");
        clientsRef = FirebaseDatabase.getInstance().getReference("clients");



        String clientId = "ghjk85555";

        // ⚡ Vérifier si ce client a déjà un conseiller
        clientsRef.child(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String myAdvisor = snapshot.child("myAdvisor").getValue(String.class);
                    String clientAgency = snapshot.child("agency").getValue(String.class); // ⚡ récupérer agence du client


                    if (myAdvisor != null && !myAdvisor.isEmpty()) {
                        // Client a déjà un conseiller → afficher seulement cet advisor
                        loadSingleAdvisor(myAdvisor);
                    } else {
                        // Pas encore de conseiller → afficher la liste complète
                        loadAllAdvisors(clientAgency);
                    }
                }
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
                        advisor.setAdvisorId(advisorId);

                        Button btn = createAdvisorButton(advisor);
                        advisorsContainer.addView(btn);
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

                        // ✅ Filtrer par agence
                        if (advisor.getAgency() != null && advisor.getAgency().equals(clientAgency)) {
                            Button btn = createAdvisorButton(advisor);
                            advisorsContainer.addView(btn);
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
                                selectedYear, (selectedMonth + 1), selectedDay);

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
                                    for (String slot : DEFAULT_SLOTS) {
                                        slots.put(slot, true);
                                    }

                                    availabilityRef.setValue(slots)
                                            .addOnSuccessListener(aVoid -> goToSlotsPage(advisor, selectedDate))
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(ClientHomeActivity.this, "Erreur lors de l'ajout de la date", Toast.LENGTH_SHORT).show());
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

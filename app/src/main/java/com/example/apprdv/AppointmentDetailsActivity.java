package com.example.apprdv;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AppointmentDetailsActivity extends AppCompatActivity {

    private TextView tvDate, tvTime, tvAdvisor, tvAgency;
    private Button btnBack;
    private DatabaseReference appointmentsRef;
    private DatabaseReference advisorsRef; // 🆕 Nécessaire pour récupérer le nom avec l'ID

    private String appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

        tvDate = findViewById(R.id.tvAppointmentDate);
        tvTime = findViewById(R.id.tvAppointmentTime);
        tvAdvisor = findViewById(R.id.tvAdvisorName);
        tvAgency = findViewById(R.id.tvAgencyName);
        btnBack = findViewById(R.id.btnBack);

        appointmentId = getIntent().getStringExtra("appointmentId");

        if (appointmentId == null) {
            Toast.makeText(this, "Erreur : ID de rendez-vous manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        advisorsRef = FirebaseDatabase.getInstance().getReference("advisors"); // 🆕 Init

        loadAppointmentData();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAppointmentData() {
        appointmentsRef.child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 1. ✅ CORRECTION DES NOMS DES CHAMPS (selon votre JSON)
                    String date = snapshot.child("selectedDate").getValue(String.class);
                    String time = snapshot.child("selectedSlot").getValue(String.class);
                    String advisorId = snapshot.child("advisorId").getValue(String.class); // On récupère l'ID

                    // Mise à jour de la date et l'heure
                    tvDate.setText(date != null ? date : "Non définie");
                    tvTime.setText(time != null ? time : "--:--");

                    // 2. ⚡ RÉCUPÉRATION DU NOM DU CONSEILLER
                    // Comme le nom n'est pas dans le RDV, on utilise l'ID pour le chercher
                    if (advisorId != null) {
                        loadAdvisorDetails(advisorId);
                    } else {
                        tvAdvisor.setText("Inconnu");
                        tvAgency.setText("Inconnue");
                    }

                } else {
                    Toast.makeText(AppointmentDetailsActivity.this, "Impossible de charger les détails.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppointmentDetailsActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🆕 Nouvelle méthode pour aller chercher les infos du conseiller
    private void loadAdvisorDetails(String advisorId) {
        advisorsRef.child(advisorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String surname = snapshot.child("surname").getValue(String.class); // Optionnel
                    String agency = snapshot.child("agency").getValue(String.class);

                    String fullName = (name != null ? name : "") + " " + (surname != null ? surname : "");

                    tvAdvisor.setText(fullName.trim().isEmpty() ? "Nom inconnu" : fullName);
                    tvAgency.setText(agency != null ? agency : "Agence inconnue");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
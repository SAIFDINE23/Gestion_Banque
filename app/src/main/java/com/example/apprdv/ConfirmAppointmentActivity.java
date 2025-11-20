package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfirmAppointmentActivity extends AppCompatActivity {

    private TextView selectedAdvisorInfo, selectedDate, selectedSlot;
    private Button confirmAppointmentButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference advisorsRef;  // Référence pour accéder aux conseillers

    private String advisorName, advisorAddress, selectedDateText, selectedSlotText, advisorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_appointment);

        selectedAdvisorInfo = findViewById(R.id.selectedAdvisorInfo);
        selectedDate = findViewById(R.id.selectedDate);
        selectedSlot = findViewById(R.id.selectedSlot);
        confirmAppointmentButton = findViewById(R.id.confirmAppointmentButton);

        // Initialiser Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("appointments");
        advisorsRef = FirebaseDatabase.getInstance().getReference("advisors");

        // Récupérer les informations envoyées via l'Intent
        advisorName = getIntent().getStringExtra("advisorName");
        advisorAddress = getIntent().getStringExtra("advisorAddress");
        selectedDateText = getIntent().getStringExtra("selectedDate");
        selectedSlotText = getIntent().getStringExtra("selectedSlot");
        advisorId = getIntent().getStringExtra("advisorId");

        // Afficher les informations dans les TextViews
        selectedAdvisorInfo.setText("Conseiller: " + advisorName + "\nAdresse: " + advisorAddress);
        selectedDate.setText("Date: " + selectedDateText);
        selectedSlot.setText("Créneau: " + selectedSlotText);

        // Bouton pour confirmer le rendez-vous
        confirmAppointmentButton.setOnClickListener(v -> confirmAppointment());
    }

    private void confirmAppointment() {
        advisorId = getIntent().getStringExtra("advisorId");
        String clientId = "ghjk85555"; // ⚡ Id du client connecté (à remplacer par mAuth si tu utilises FirebaseAuth)

        String appointmentId = mDatabase.push().getKey();
        if (appointmentId != null) {
            // ✅ Créer un Appointment avec l'ID
            Appointment appointment = new Appointment(
                    appointmentId,
                    advisorId,
                    clientId,
                    selectedDateText,
                    selectedSlotText,
                    "Confirmé"
            );

            // Sauvegarder dans la collection "appointments"
            mDatabase.child(appointmentId).setValue(appointment.toMap()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DatabaseReference clientRef = FirebaseDatabase.getInstance()
                            .getReference("clients")
                            .child(clientId);

                    // ⚡ Mettre à jour myAdvisor si c'est la première fois
                    clientRef.child("myAdvisor").get().addOnSuccessListener(snapshot -> {
                        String existingAdvisor = snapshot.getValue(String.class);
                        if (existingAdvisor == null || existingAdvisor.isEmpty()) {
                            clientRef.child("myAdvisor").setValue(advisorId);
                        }
                    });

                    // ⚡ Mettre à jour appointmentId dans le client
                    clientRef.child("appointmentId").setValue(appointmentId);

                    // ⚡ Mettre à jour la disponibilité de l’advisor
                    updateAdvisorAvailability(advisorId, selectedDateText, selectedSlotText);

                    Toast.makeText(ConfirmAppointmentActivity.this, "Rendez-vous confirmé avec succès", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ConfirmAppointmentActivity.this, ClientHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ConfirmAppointmentActivity.this, "Erreur lors de la confirmation du rendez-vous", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Méthode pour mettre à jour la disponibilité du créneau dans la base de données Firebase
    private void updateAdvisorAvailability(String advisorId, String selectedDate, String selectedSlot) {
        DatabaseReference advisorRef = advisorsRef.child(advisorId).child("availability").child(selectedDate);

        advisorRef.child(selectedSlot).setValue(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                advisorRef.child(selectedSlot).get().addOnCompleteListener(snapshotTask -> {
                    if (snapshotTask.isSuccessful()) {
                        Boolean updatedValue = snapshotTask.getResult().getValue(Boolean.class);
                        if (updatedValue != null && !updatedValue) {
                            Toast.makeText(ConfirmAppointmentActivity.this, "Disponibilité mise à jour à 'false' pour " + selectedDate + " (" + advisorId + ")", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ConfirmAppointmentActivity.this, "Erreur de mise à jour de la disponibilité", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ConfirmAppointmentActivity.this, "Erreur de récupération des données", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ConfirmAppointmentActivity.this, "Erreur de mise à jour de la disponibilité", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

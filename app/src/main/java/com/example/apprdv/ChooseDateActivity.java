package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChooseDateActivity extends AppCompatActivity {

    private TextView txtAdvisorInfo, txtDateTime;
    private Button btnConfirm;

    private String advisorId, advisorName, advisorAddress, advisorAgency, selectedDateTime;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_date);

        // ⚡ Assure-toi qu’il n’y a PAS "import android.R;" en haut de ton fichier

        txtAdvisorInfo = findViewById(R.id.txtAdvisorInfo);
        txtDateTime = findViewById(R.id.txtDateTime);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Récupérer les données envoyées depuis ClientHomeActivity
        Intent intent = getIntent();
        advisorId = intent.getStringExtra("advisorId");   // ✅ Utiliser l’ID Firebase du conseiller
        advisorName = intent.getStringExtra("advisorName");
        advisorAddress = intent.getStringExtra("advisorAddress");
        advisorAgency = intent.getStringExtra("advisorAgency");
        selectedDateTime = intent.getStringExtra("selectedDateTime");

        // Afficher les infos
        txtAdvisorInfo.setText("Conseiller : " + advisorName +
                "\nAdresse : " + advisorAddress +
                "\nAgence : " + advisorAgency);
        txtDateTime.setText("Date et heure choisies : " + selectedDateTime);

        // Référence Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("advisors");

        btnConfirm.setOnClickListener(v -> confirmerRdv());
    }

    private void confirmerRdv() {
        if (selectedDateTime == null || advisorId == null) {
            Toast.makeText(this, "Erreur : données manquantes", Toast.LENGTH_SHORT).show();
            return;
        }

        // Exemple : "2025-09-20 à 14:30"
        String[] parts = selectedDateTime.split(" à ");
        if (parts.length != 2) {
            Toast.makeText(this, "Format de date invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = parts[0];
        String hour = parts[1];

        // ✅ Utiliser l’ID du conseiller comme clé (plus sûr que le nom)
        DatabaseReference ref = mDatabase.child(advisorId)
                .child("availability")
                .child(date)
                .child(hour);

        // Mettre à jour dans Firebase : false = réservé
        ref.setValue(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ChooseDateActivity.this,
                        "Rendez-vous confirmé avec " + advisorName,
                        Toast.LENGTH_LONG).show();
                finish(); // Retour à l’écran précédent
            } else {
                Toast.makeText(ChooseDateActivity.this,
                        "Erreur Firebase : " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

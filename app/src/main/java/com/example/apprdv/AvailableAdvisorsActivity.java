package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AvailableAdvisorsActivity extends AppCompatActivity {

    private RecyclerView availableAdvisorsRecyclerView;
    private List<Advisor> availableAdvisorsList = new ArrayList<>();
    private String selectedDate;
    private String clientAgency; // l'agence du client connecté

    private AvailableAdvisorsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_advisors);

        availableAdvisorsRecyclerView = findViewById(R.id.availableAdvisorsRecyclerView);

        // Récupération de la date sélectionnée et de l'agence du client
        selectedDate = getIntent().getStringExtra("selectedDate");
        clientAgency = getIntent().getStringExtra("clientAgency");

        adapter = new AvailableAdvisorsAdapter(availableAdvisorsList, this::onAdvisorSlotSelected);
        availableAdvisorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        availableAdvisorsRecyclerView.setAdapter(adapter);

        // Charger les conseillers disponibles à la date choisie
        getAvailableAdvisors(selectedDate, clientAgency);
    }

    private void getAvailableAdvisors(String date, String clientAgency) {
        DatabaseReference advisorsRef = FirebaseDatabase.getInstance().getReference("advisors");

        advisorsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                availableAdvisorsList.clear(); // éviter les doublons

                DataSnapshot snapshot = task.getResult();
                for (DataSnapshot advisorSnapshot : snapshot.getChildren()) {

                    // Lire les infos de base (valeurs telles qu'elles existent dans la DB)
                    String advisorNodeKey = advisorSnapshot.getKey(); // clé Firebase (ex: -OZpTWIz-...)
                    String advisorIdField = advisorSnapshot.child("id").getValue(String.class); // champ "id"
                    String advisorName = advisorSnapshot.child("name").getValue(String.class);
                    String advisorSurname = advisorSnapshot.child("surname").getValue(String.class);
                    String advisorAgency = advisorSnapshot.child("agency").getValue(String.class);
                    String advisorPhone = advisorSnapshot.child("phone").getValue(String.class);
                    String advisorAddress = advisorSnapshot.child("address").getValue(String.class);
                    String advisorEmail = advisorSnapshot.child("email").getValue(String.class);
                    String advisorPassword = advisorSnapshot.child("password").getValue(String.class);
                    String advisorRole = advisorSnapshot.child("role").getValue(String.class);

                    // ⚠️ Filtrer par agence du client (si fournie)
                    if (clientAgency != null && advisorAgency != null) {
                        if (!advisorAgency.equalsIgnoreCase(clientAgency)) {
                            continue;
                        }
                    }

                    // Vérifier si le conseiller a une disponibilité à la date donnée
                    if (advisorSnapshot.child("availability").hasChild(date)) {
                        DataSnapshot availabilitySnapshot = advisorSnapshot.child("availability").child(date);
                        List<String> availableSlots = new ArrayList<>();

                        for (DataSnapshot timeSlotSnapshot : availabilitySnapshot.getChildren()) {
                            Boolean isAvailable = timeSlotSnapshot.getValue(Boolean.class);
                            if (Boolean.TRUE.equals(isAvailable)) {
                                availableSlots.add(timeSlotSnapshot.getKey());
                            }
                        }

                        // Ajouter le conseiller si au moins un créneau est disponible
                        if (!availableSlots.isEmpty()) {
                            // Récupérer la map complète d'availability si besoin
                            @SuppressWarnings("unchecked")
                            Map<String, Map<String, Boolean>> availability =
                                    (Map<String, Map<String, Boolean>>) advisorSnapshot.child("availability").getValue();

                            // --- UTILISER CONSTRUCTEUR VIDE + SETTERS (robuste) ---
                            Advisor advisor = new Advisor(); // doit exister (constructeur vide)
                            // assigner la clé firebase comme advisorId (utile pour mises à jour)
                            advisor.setAdvisorId(advisorNodeKey);

                            // si ta classe a setId / setName etc., elles seront appelées
                            if (hasMethod_setId(advisor)) advisor.setId(advisorIdField);
                            advisor.setName(advisorName);
                            advisor.setSurname(advisorSurname);
                            advisor.setAgency(advisorAgency);
                            advisor.setPhone(advisorPhone);
                            advisor.setAddress(advisorAddress);
                            advisor.setEmail(advisorEmail);
                            advisor.setPassword(advisorPassword);
                            advisor.setRole(advisorRole);

                            advisor.setAvailableSlots(availableSlots);
                            advisor.setAvailability(availability);

                            availableAdvisorsList.add(advisor);
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                if (availableAdvisorsList.isEmpty()) {
                    Toast.makeText(this, "Aucun conseiller disponible pour cette date.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Erreur de récupération des conseillers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lorsqu’un créneau est sélectionné
    private void onAdvisorSlotSelected(Advisor advisor, String selectedSlot) {
        Intent intent = new Intent(this, ConfirmAppointmentActivity.class);
        intent.putExtra("advisorName", advisor.getName());
        intent.putExtra("advisorAddress", advisor.getAddress());
        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("selectedSlot", selectedSlot);

        // on envoie la clé firebase (advisorId) si disponible
        intent.putExtra("advisorId", advisor.getAdvisorId() != null ? advisor.getAdvisorId() : advisor.getId());
        startActivity(intent);
    }

    /**
     * Petit hack : vérifier si la classe Advisor expose setId (pour compatibilité).
     * Ici on utilise reflection uniquement pour éviter l'APi error si ces setters n'existent pas.
     * (Tu peux enlever ces checks si ta classe Advisor a bien tous les setters.)
     */
    private boolean hasMethod_setId(Advisor adv) {
        try {
            adv.getClass().getMethod("setId", String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}

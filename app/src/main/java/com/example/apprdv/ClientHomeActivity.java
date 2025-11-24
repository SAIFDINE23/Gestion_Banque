package com.example.apprdv;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Locale;
import java.util.Map;

public class ClientHomeActivity extends AppCompatActivity {

    private LinearLayout advisorsContainer;
    private DatabaseReference mDatabase; // Référence vers advisors
    private DatabaseReference clientsRef;
    private DatabaseReference appointmentsRef;

    private static final String[] DEFAULT_SLOTS = {"10:00", "11:00", "12:00", "14:00", "15:00", "16:00"};
    private String currentClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);

        advisorsContainer = findViewById(R.id.advisorsContainer);
        mDatabase = FirebaseDatabase.getInstance().getReference("advisors");
        clientsRef = FirebaseDatabase.getInstance().getReference("clients");
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        currentClientId = getIntent().getStringExtra("clientId");
        if (currentClientId == null) {
            Toast.makeText(this, "Erreur : client introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button btnChangeAdvisor = findViewById(R.id.btnChangeAdvisor);
        Button btnViewRequests = findViewById(R.id.btnViewRequests);

        // --- Logique d'affichage des boutons (Ancienneté) ---
        clientsRef.child(currentClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String creationDateStr = snapshot.child("creationDate").getValue(String.class);
                String myAdvisor = snapshot.child("myAdvisor").getValue(String.class);
                String clientAgency = snapshot.child("agency").getValue(String.class);

                boolean showButtons = false;

                if (creationDateStr != null && !creationDateStr.isEmpty()) {
                    // Utilisation du format français dd/MM/yyyy
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                    try {
                        Date creationDate = sdf.parse(creationDateStr);
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.YEAR, -1);
                        Date oneYearAgo = cal.getTime();

                        if (creationDate != null && creationDate.before(oneYearAgo)) {
                            showButtons = true;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                btnChangeAdvisor.setVisibility(showButtons ? Button.VISIBLE : Button.GONE);
                btnViewRequests.setVisibility(showButtons ? Button.VISIBLE : Button.GONE);

                if (myAdvisor != null && !myAdvisor.isEmpty()) {
                    loadSingleAdvisor(myAdvisor);
                } else {
                    loadAllAdvisors(clientAgency);
                }

                btnChangeAdvisor.setOnClickListener(v -> {
                    Intent intent = new Intent(ClientHomeActivity.this, ChangeAdvisorFormActivity.class);
                    intent.putExtra("clientId", currentClientId);
                    startActivity(intent);
                });

                btnViewRequests.setOnClickListener(v -> {
                    Intent intent = new Intent(ClientHomeActivity.this, MyRequestActivity.class);
                    intent.putExtra("clientId", currentClientId);
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

        // C'est ICI que ça se passe
        btn.setOnClickListener(v -> {
            // Toast de debug pour voir si le clic marche
            Toast.makeText(ClientHomeActivity.this, "Vérification en cours...", Toast.LENGTH_SHORT).show();
            checkExistingAppointmentAndBook(advisor);
        });

        return btn;
    }

    private void checkExistingAppointmentAndBook(Advisor advisor) {
        clientsRef.child(currentClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String appointmentId = snapshot.child("appointmentId").getValue(String.class);

                // Toast de debug
                // Toast.makeText(ClientHomeActivity.this, "Appt ID: " + appointmentId, Toast.LENGTH_SHORT).show();

                if (appointmentId == null || appointmentId.isEmpty()) {
                    showDatePicker(advisor);
                } else {
                    verifyAppointmentDate(appointmentId, advisor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClientHomeActivity.this, "Erreur BD Client", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyAppointmentDate(String appointmentId, Advisor advisor) {
        appointmentsRef.child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showDatePicker(advisor);
                    return;
                }

                // CORRECTION 1 : Utiliser le bon nom de champ "selectedDate"
                String dateRdvStr = snapshot.child("selectedDate").getValue(String.class);

                // Si ça ne marche pas, essayez "date" (pour gérer les anciens formats)
                if (dateRdvStr == null) {
                    dateRdvStr = snapshot.child("date").getValue(String.class);
                }

                if (dateRdvStr != null) {
                    try {
                        Date dateRdv;

                        // CORRECTION 2 : Gérer le format de la base de données (2025-11-26)
                        if (dateRdvStr.contains("-")) {
                            // Format yyyy-MM-dd
                            SimpleDateFormat sdfISO = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                            dateRdv = sdfISO.parse(dateRdvStr);
                        } else {
                            // Format dd/MM/yyyy (votre format de sauvegarde actuel)
                            SimpleDateFormat sdfFR = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                            dateRdv = sdfFR.parse(dateRdvStr);
                        }

                        // Créer la date d'aujourd'hui à 00h00
                        Calendar calToday = Calendar.getInstance();
                        calToday.set(Calendar.HOUR_OF_DAY, 0);
                        calToday.set(Calendar.MINUTE, 0);
                        calToday.set(Calendar.SECOND, 0);
                        calToday.set(Calendar.MILLISECOND, 0);
                        Date todayZero = calToday.getTime();

                        // LOGIQUE : Si le RDV est avant aujourd'hui, c'est du passé -> OK
                        if (dateRdv.before(todayZero)) {
                            showDatePicker(advisor);
                        } else {
                            // RDV aujourd'hui ou futur -> BLOQUER
                            Toast.makeText(ClientHomeActivity.this, "Rendez-vous existant le " + dateRdvStr, Toast.LENGTH_LONG).show();
                            goToAppointmentDetails(appointmentId);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                        // En cas d'erreur de lecture de date, on ouvre par sécurité
                        showDatePicker(advisor);
                    }
                } else {
                    // Pas de date trouvée dans le RDV -> on laisse réserver
                    showDatePicker(advisor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClientHomeActivity.this, "Erreur BD RDV", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToAppointmentDetails(String appointmentId) {
        Intent intent = new Intent(ClientHomeActivity.this, AppointmentDetailsActivity.class);
        intent.putExtra("appointmentId", appointmentId);
        startActivity(intent);
    }

    private void showDatePicker(Advisor advisor) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ClientHomeActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar chosenDate = Calendar.getInstance();
                    chosenDate.set(selectedYear, selectedMonth, selectedDay);

                    // Bloquer les dates passées
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    if (chosenDate.before(today)) {
                        Toast.makeText(ClientHomeActivity.this, "Date invalide (passée)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 🔥 On stocke au format dd/MM/yyyy pour être cohérent
                    String selectedDate = String.format(Locale.FRANCE, "%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear);

                    checkAvailabilityAndBook(advisor, selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void checkAvailabilityAndBook(Advisor advisor, String selectedDate) {
        // Attention : Firebase n'aime pas les "/" dans les clés (ex: availability/24/11/2025)
        // Il faut remplacer les "/" par des "-" pour la clé Firebase
        String firebaseDateKey = selectedDate.replace("/", "-");

        DatabaseReference availabilityRef = FirebaseDatabase.getInstance()
                .getReference("advisors")
                .child(advisor.getAdvisorId())
                .child("availability")
                .child(firebaseDateKey); // Utilisation de la clé formatée

        availabilityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Boolean> slots = new HashMap<>();
                    for (String slot : DEFAULT_SLOTS) slots.put(slot, true);
                    availabilityRef.setValue(slots)
                            .addOnSuccessListener(aVoid -> goToSlotsPage(advisor, selectedDate))
                            .addOnFailureListener(e -> Toast.makeText(ClientHomeActivity.this, "Erreur création créneaux", Toast.LENGTH_SHORT).show());
                } else {
                    goToSlotsPage(advisor, selectedDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void goToSlotsPage(Advisor advisor, String selectedDate) {
        Intent intent = new Intent(ClientHomeActivity.this, ChooseSlotActivity.class);
        intent.putExtra("advisorName", advisor.getName());
        intent.putExtra("advisorId", advisor.getAdvisorId());
        intent.putExtra("selectedDate", selectedDate);
        startActivity(intent);
    }
}
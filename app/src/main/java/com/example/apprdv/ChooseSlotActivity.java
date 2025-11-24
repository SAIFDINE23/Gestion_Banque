package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChooseSlotActivity extends AppCompatActivity {

    private String advisorId, advisorName, selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cette activité n'a pas de layout setContentView car elle affiche juste une Dialog
        // Mais pour éviter le crash, on laisse souvent un layout vide ou on gère via Dialog directe

        advisorId = getIntent().getStringExtra("advisorId");
        advisorName = getIntent().getStringExtra("advisorName");
        selectedDate = getIntent().getStringExtra("selectedDate"); // Ici c'est "26/11/2025"

        // 🔴 CORRECTION IMPORTANTE : Formater la date pour Firebase
        // On transforme "26/11/2025" en "26-11-2025" pour correspondre à la clé de la BD
        String firebaseDateKey = selectedDate.replace("/", "-");

        DatabaseReference availabilityRef = FirebaseDatabase.getInstance()
                .getReference("advisors")
                .child(advisorId)
                .child("availability")
                .child(firebaseDateKey); // ✅ On utilise la clé avec tirets

        availabilityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> allHours = new ArrayList<>();
                List<String> unavailableHours = new ArrayList<>();

                for (DataSnapshot hourSnap : snapshot.getChildren()) {
                    Boolean isAvailable = hourSnap.getValue(Boolean.class);
                    String hour = hourSnap.getKey();

                    if (isAvailable == null || isAvailable) {
                        allHours.add(hour);
                    } else {
                        allHours.add(hour);
                        unavailableHours.add(hour);
                    }
                }

                if (allHours.isEmpty()) {
                    // C'est ici que vous tombiez avant car le chemin était faux
                    Toast.makeText(ChooseSlotActivity.this,
                            "Aucune heure définie pour ce jour",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            ChooseSlotActivity.this,
                            android.R.layout.simple_list_item_1,
                            allHours) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView tv = view.findViewById(android.R.id.text1);
                            String hour = getItem(position);
                            if (unavailableHours.contains(hour)) {
                                tv.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            } else {
                                tv.setTextColor(getResources().getColor(android.R.color.black));
                            }
                            return view;
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(ChooseSlotActivity.this);
                    builder.setTitle("Choisissez une heure le " + selectedDate);
                    builder.setAdapter(adapter, (dialog, which) -> {
                        String selectedHour = allHours.get(which);

                        if (unavailableHours.contains(selectedHour)) {
                            Toast.makeText(ChooseSlotActivity.this,
                                    "Cette heure n'est pas disponible",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Redirection vers la confirmation
                            Intent intent = new Intent(ChooseSlotActivity.this, ConfirmAppointmentActivity.class);
                            intent.putExtra("advisorName", advisorName);
                            intent.putExtra("advisorId", advisorId);
                            intent.putExtra("selectedDate", selectedDate);   // On garde le format visuel (/)
                            intent.putExtra("selectedSlot", selectedHour);
                            startActivity(intent);
                            finish(); // On ferme la dialog activity
                        }
                    });
                    builder.setOnCancelListener(dialog -> finish()); // Fermer si on annule
                    builder.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
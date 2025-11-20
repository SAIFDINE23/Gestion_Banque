package com.example.apprdv;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClientDetailsActivity extends AppCompatActivity {

    private TextView tvClientFullName, tvClientAgency, tvClientPhone, tvClientAddress, tvClientBirthday,tvClientid,tvClientAdvisor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_details);

        tvClientFullName = findViewById(R.id.tvClientFullName);
        tvClientAgency = findViewById(R.id.tvClientAgency);
        tvClientPhone = findViewById(R.id.tvClientPhone);
        tvClientAddress = findViewById(R.id.tvClientAddress);
        tvClientBirthday = findViewById(R.id.tvClientBirthday);
        tvClientid = findViewById(R.id.tvClientid);
        tvClientAdvisor = findViewById(R.id.tvClientAdvisor);



        String clientId = getIntent().getStringExtra("CLIENT_ID");
        loadClientDetails(clientId);
    }

    private void loadClientDetails(String clientId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("clients");
        ref.orderByChild("id").equalTo(clientId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String name = ds.child("name").getValue(String.class);
                                String surname = ds.child("surname").getValue(String.class);
                                String agency = ds.child("agency").getValue(String.class);
                                String phone = ds.child("teld").getValue(String.class);
                                String address = ds.child("adress").getValue(String.class);
                                String id = ds.child("idd").getValue(String.class);
                                String advisorId = ds.child("myAdvisor").getValue(String.class);

                                tvClientFullName.setText(name + " " + surname);
                                tvClientAgency.setText("Agence: " + agency);
                                tvClientPhone.setText("Téléphone: " + (phone != null ? phone : "032 123 456"));
                                tvClientAddress.setText("Adresse: " + (address != null ? address : "656 Avenue de Toumaniantz"));
                                tvClientid.setText("ID: " + (id != null ? id : "sd2601200399"));

                                // 🔹 récupérer le nom de l’advisor depuis son id
                                if (advisorId != null && !advisorId.isEmpty()) {
                                    DatabaseReference advisorRef = FirebaseDatabase.getInstance()
                                            .getReference("advisors").child(advisorId);
                                    advisorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot advisorSnap) {
                                            if (advisorSnap.exists()) {
                                                String advisorName = advisorSnap.child("name").getValue(String.class);
                                                tvClientAdvisor.setText("Advisor: " + (advisorName != null ? advisorName : "Non défini"));
                                            } else {
                                                tvClientAdvisor.setText("Advisor: Non défini");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            tvClientAdvisor.setText("Advisor: Non défini");
                                        }
                                    });
                                } else {
                                    tvClientAdvisor.setText("Advisor: Non défini");
                                }
                            }
                        }
                        else {
                            Toast.makeText(ClientDetailsActivity.this, "Client introuvable", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ClientDetailsActivity.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserId;
    private Button btnLogin;
    private DatabaseReference mDatabase;
    private String role; // "client" ou "advisor"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUserId = findViewById(R.id.etUserId);
        btnLogin = findViewById(R.id.btnLogin);

        // Récupérer le rôle envoyé depuis RoleSelectionActivity
        role = getIntent().getStringExtra("role");

        // Initialiser la bonne collection selon le rôle
        if ("advisor".equalsIgnoreCase(role)) {
            mDatabase = FirebaseDatabase.getInstance().getReference("advisors");
        } else {
            mDatabase = FirebaseDatabase.getInstance().getReference("clients");
        }

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String userId = etUserId.getText().toString().trim();

        if (userId.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier si l'utilisateur existe
        mDatabase.orderByChild("id").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    Boolean firstConnection = userSnapshot.child("firstConnection").getValue(Boolean.class);

                    if (firstConnection == null) firstConnection = false;

                    if (firstConnection) {
                        String generatedPassword = generateRandomPassword();

                        // Mise à jour du mot de passe et du flag
                        mDatabase.child(userSnapshot.getKey()).child("password").setValue(generatedPassword);
                        mDatabase.child(userSnapshot.getKey()).child("firstConnection").setValue(false)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(LoginActivity.this, ConfirmPasswordActivity.class);
                                        intent.putExtra("password", generatedPassword);
                                        intent.putExtra("role", role);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    } else {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        Intent intent = new Intent(LoginActivity.this, EnterPasswordActivity.class);
                        intent.putExtra("storedPassword", storedPassword);
                        intent.putExtra("role", role);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "ID incorrect", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Erreur de connexion à la base de données", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return password.toString();
    }
}

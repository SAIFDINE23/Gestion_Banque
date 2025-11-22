package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EnterPasswordActivity extends AppCompatActivity {

    private EditText etPassword;
    private Button btnConfirm;
    private String advisorId;
    private String clientId;


    private String storedPassword;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        etPassword = findViewById(R.id.etPassword);
        btnConfirm = findViewById(R.id.btnConfirm);
        advisorId = getIntent().getStringExtra("advisorId");
        clientId = getIntent().getStringExtra("clientId");



        storedPassword = getIntent().getStringExtra("storedPassword");
        role = getIntent().getStringExtra("role"); // récupéré depuis LoginActivity

        btnConfirm.setOnClickListener(v -> confirmPassword());
    }

    private void confirmPassword() {
        String enteredPassword = etPassword.getText().toString().trim();

        if (enteredPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer le mot de passe", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredPassword.equals(storedPassword)) {
            Intent intent;
            if ("advisor".equalsIgnoreCase(role)) {
                intent = new Intent(this, AdvisorHomeActivity.class);
                intent.putExtra("advisorId", advisorId); // si c’est un conseiller
            } else {
                intent = new Intent(this, ClientHomeActivity.class);
                intent.putExtra("clientId", clientId); // <-- REENVOYÉ ICI
            }

            startActivity(intent);
            finish();
        }
        else {
            Toast.makeText(this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}

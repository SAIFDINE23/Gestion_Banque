package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ConfirmPasswordActivity extends AppCompatActivity {

    private TextView tvPassword;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_password);

        // Initialiser les vues
        tvPassword = findViewById(R.id.tvPassword);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Récupérer le mot de passe généré passé depuis LoginActivity
        String generatedPassword = getIntent().getStringExtra("password");

        // Afficher le mot de passe dans un TextView
        tvPassword.setText("Votre mot de passe généré est : " + generatedPassword);

        // Lorsque l'utilisateur confirme, il est redirigé vers la page de login
        btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(ConfirmPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

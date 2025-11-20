package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    private Button btnClient, btnAdvisor;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        welcomeText = findViewById(R.id.tvWelcome);
        btnClient = findViewById(R.id.btnClient);
        btnAdvisor = findViewById(R.id.btnAdvisor);

        welcomeText.setText("Welcome to Your Bank");

        // Si l'utilisateur choisit "Client"
        btnClient.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
            intent.putExtra("role", "client");
            startActivity(intent);
        });

        // Si l'utilisateur choisit "Advisor"
        btnAdvisor.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
            intent.putExtra("role", "advisor");
            startActivity(intent);
        });
    }
}

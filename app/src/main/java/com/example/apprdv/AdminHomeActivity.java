package com.example.apprdv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Initialiser le bouton "Ajouter un conseiller"
        Button addAdvisorButton = findViewById(R.id.btn_add_advisor);

        // Ajouter un écouteur de clic pour le bouton
        addAdvisorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lorsqu'on clique sur le bouton, on va rediriger vers la page pour ajouter un conseiller
                Intent intent = new Intent(AdminHomeActivity.this, AddAdvisorActivity.class);
                startActivity(intent);
            }
        });
    }
}

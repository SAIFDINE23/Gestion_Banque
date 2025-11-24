package com.example.apprdv;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ClientHomeUITest {

    // ================================================================
    // 🔧 DONNÉES DE TEST (Basées sur votre BD)
    // ================================================================

    // CAS 1 : Client RÉCENT (< 1 an)
    // ID : Uythhughuhv
    // creationDate : "23/10/2024"
    // Hypothèse : Nous sommes en 2024 ou début 2025. Donc c'est < 1 an.
    private static final String ID_CLIENT_RECENT = "Uythhughuhv";

    // CAS 2 : Client ANCIEN (> 1 an)
    // ID : ghjk85555 (Celui de l'exemple précédent)
    // creationDate : "26/01/2003" (ou n'importe quelle date avant 2023)
    private static final String ID_CLIENT_ANCIEN = "ghjk85555";


    // ================================================================
    // 🧪 TESTS UI ESPRESSO
    // ================================================================

    /**
     * TEST A : Vérifie que le client RÉCENT (Uythhughuhv) ne voit PAS les boutons.
     * Condition : Date actuelle - 23/10/2024 < 1 an
     */
    @Test
    public void testButtonsHiddenForRecentClient() throws InterruptedException {
        // 1. Préparer l'Intent avec l'ID du client récent
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClientHomeActivity.class);
        intent.putExtra("clientId", ID_CLIENT_RECENT);

        // 2. Lancer l'activité directement
        try (ActivityScenario<ClientHomeActivity> scenario = ActivityScenario.launch(intent)) {

            // 3. Attendre que Firebase charge les données (Asynchrone)
            // C'est nécessaire car l'UI se met à jour après la réponse de Firebase
            Thread.sleep(3000);

            // 4. VÉRIFICATION : Les boutons doivent être invisibles (GONE)
            onView(withId(R.id.btnChangeAdvisor))
                    .check(matches(not(isDisplayed())));

            onView(withId(R.id.btnViewRequests))
                    .check(matches(not(isDisplayed())));
        }
    }

    /**
     * TEST B : Vérifie que le client ANCIEN (ghjk85555) VOIT les boutons.
     * Condition : Date actuelle - 2003 > 1 an
     */
    @Test
    public void testButtonsVisibleForOldClient() throws InterruptedException {
        // 1. Préparer l'Intent avec l'ID du client ancien
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClientHomeActivity.class);
        intent.putExtra("clientId", ID_CLIENT_ANCIEN);

        // 2. Lancer l'activité
        try (ActivityScenario<ClientHomeActivity> scenario = ActivityScenario.launch(intent)) {

            // 3. Attendre le chargement
            Thread.sleep(3000);

            // 4. VÉRIFICATION : Les boutons doivent être affichés (VISIBLE)
            onView(withId(R.id.btnChangeAdvisor))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.btnViewRequests))
                    .check(matches(isDisplayed()));
        }
    }
}
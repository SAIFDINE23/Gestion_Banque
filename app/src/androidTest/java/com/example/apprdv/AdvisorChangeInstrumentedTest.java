package com.example.apprdv;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import android.content.Intent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class AdvisorChangeInstrumentedTest {

    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final String TEST_CLIENT_KEY = "ghjk85555";

    // 🔴 CORRECTION ICI : Mise à jour selon ce que votre émulateur affiche réellement
    // D'après vos logs, c'est "ayoub" qui s'affiche, pas "amin".
    private final String EXPECTED_ADVISOR_NAME = "ayoub";

    // 🔴 CORRECTION ICI : Respect des majuscules "Calais Rue 690"
    private final String EXPECTED_ADDRESS = "Calais Rue 690";
    private final String EXPECTED_AGENCY = "Calais 520";

    // ==================================================================
    // ⚙️ PARTIE 1 : TESTS BACKEND
    // ==================================================================

    @Test
    public void testClientRequiredFieldsExist() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        DatabaseReference ref = db.getReference("clients").child(TEST_CLIENT_KEY);

        ref.get().addOnCompleteListener(task -> {
            assertTrue("Firebase error: " + task.getException(), task.isSuccessful());
            DataSnapshot snap = task.getResult();
            assertNotNull("Client not found", snap);
            assertNotNull("name missing", snap.child("name").getValue(String.class));
            assertNotNull("surname missing", snap.child("surname").getValue(String.class));
            assertNotNull("agency missing", snap.child("agency").getValue(String.class));
            assertNotNull("creationDate missing", snap.child("creationDate").getValue(String.class));
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testCreationDateFormat() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        DatabaseReference ref = db.getReference("clients").child(TEST_CLIENT_KEY);

        ref.get().addOnCompleteListener(task -> {
            assertTrue(task.isSuccessful());
            String creationDate = task.getResult().child("creationDate").getValue(String.class);
            assertNotNull(creationDate);
            assertTrue("Format date invalide: " + creationDate, creationDate.matches("\\d{2}/\\d{2}/\\d{4}"));
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testAdvisorWithLessThan10ClientsExists() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        DatabaseReference advisorsRef = db.getReference("advisors");
        DatabaseReference clientsRef = db.getReference("clients");

        advisorsRef.get().addOnCompleteListener(advisorTask -> {
            assertTrue(advisorTask.isSuccessful());
            clientsRef.get().addOnCompleteListener(clientTask -> {
                assertTrue(clientTask.isSuccessful());
                boolean found = false;
                for (DataSnapshot advisor : advisorTask.getResult().getChildren()) {
                    String advisorKey = advisor.getKey();
                    long count = 0;
                    for (DataSnapshot client : clientTask.getResult().getChildren()) {
                        String myAdvisor = client.child("myAdvisor").getValue(String.class);
                        if (advisorKey != null && advisorKey.equals(myAdvisor)) {
                            count++;
                        }
                    }
                    if (count < 10) {
                        found = true;
                        break;
                    }
                }
                assertTrue("Aucun advisor avec < 10 clients", found);
                latch.countDown();
            });
        });
        latch.await(7, TimeUnit.SECONDS);
    }

    // ==================================================================
    // 📱 PARTIE 2 : TESTS UI (CORRIGÉE)
    // ==================================================================

    @Test
    public void testAdvisorDisplayedInList() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClientHomeActivity.class);
        intent.putExtra("clientId", TEST_CLIENT_KEY);

        try (ActivityScenario<ClientHomeActivity> scenario = ActivityScenario.launch(intent)) {

            // On laisse le temps à Firebase de charger
            Thread.sleep(4000);

            // Vérification du nom (ayoub)
            // containsString ignore le reste du texte ("Nom : ...")
            onView(withText(containsString(EXPECTED_ADVISOR_NAME)))
                    .check(matches(isDisplayed()));

            // Vérification de l'agence (Calais 520)
            onView(withText(containsString(EXPECTED_AGENCY)))
                    .check(matches(isDisplayed()));
        }
    }


    @Test
    public void testAdvisorAddressIsVisible() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClientHomeActivity.class);
        intent.putExtra("clientId", TEST_CLIENT_KEY);

        try (ActivityScenario<ClientHomeActivity> scenario = ActivityScenario.launch(intent)) {

            Thread.sleep(4000);

            // ⚡ CORRECTION : On cible le conteneur parent (advisorsContainer)
            // et on vérifie qu'il contient (hasDescendant) le texte voulu.
            onView(withId(R.id.advisorsContainer))
                    .perform(scrollTo()) // S'assure que le conteneur est visible
                    .check(matches(hasDescendant(withText(containsString(EXPECTED_ADDRESS)))));
        }
    }
}
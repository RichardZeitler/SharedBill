package htw.university.sharedbill;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import htw.university.sharedbill.controller.StartActivity;

/**
 * Instrumentationstest zum Überprüfen, ob die CreateInvoiceActivity
 * korrekt angezeigt wird, wenn der entsprechende Button geklickt wird.
 */
@RunWith(AndroidJUnit4.class)
public class DisplayCreateInvoiceActivityTest {

    /**
     * Startet die StartActivity vor jedem Test.
     */
    @Rule
    public ActivityScenarioRule<StartActivity> activityRule = new ActivityScenarioRule<>(StartActivity.class);

    /**
     * Testet, ob nach dem Klick auf den Button mit der ID addInvoiceButton
     * die Ansicht mit der ID addItem angezeigt wird.
     */
    @Test
    public void testDisplayCreateInvoiceActivity() {
        // Klick auf den Button, um CreateInvoiceActivity zu starten
        onView(withId(R.id.addInvoiceButton)).perform(click());

        // Überprüft, ob die View mit der ID addItem sichtbar ist
        onView(withId(R.id.addItem))
                .check(matches(isDisplayed()));
    }

}

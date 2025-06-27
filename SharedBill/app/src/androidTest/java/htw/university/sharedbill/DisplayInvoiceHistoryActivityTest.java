package htw.university.sharedbill;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import htw.university.sharedbill.controller.invoce.StartActivity;

/**
 * Instrumentationstest für das Öffnen der Rechnungshistorie
 * und die Prüfung, ob der Header korrekt angezeigt wird.
 */
@RunWith(AndroidJUnit4.class)
public class DisplayInvoiceHistoryActivityTest {

    /**
     * Startet die StartActivity vor jedem Test.
     */
    @Rule
    public ActivityScenarioRule<StartActivity> activityRule = new ActivityScenarioRule<>(StartActivity.class);

    /**
     * Testet, ob durch Klick auf den Button mit der ID invoiceHistoryButton
     * die Rechnungshistorie geöffnet wird und der Header mit Text "Rechnungshistorie" angezeigt wird.
     */
    @Test
    public void testDisplayInvoiceHistoryActivity() {
        // Klick auf den Button, der die Rechnungshistorie öffnet
        onView(withId(R.id.invoiceHistoryButton)).perform(click());

        // Überprüft, ob das TextView mit der ID invoiceHeader den Text "Rechnungshistorie" zeigt
        onView(withId(R.id.invoiceHeader))
                .check(matches(withText("Rechnungshistorie")));
    }
}

package htw.university.sharedbill;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

import htw.university.sharedbill.controller.invoce.StartActivity;
import htw.university.sharedbill.model.invoice.Address;
import htw.university.sharedbill.model.invoice.EateryInvoice;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;
import htw.university.sharedbill.model.invoice.Item;

/**
 * Instrumentationstest für den vollständigen Flow vom StartActivity
 * über das Anzeigen einer Rechnung in InvoiceShowActivity.
 *
 * Testet insbesondere:
 * - Öffnen der InvoiceHistoryActivity
 * - Öffnen des Popup-Menüs in einem RecyclerView-Item
 * - Auswahl des Menüeintrags "Anzeigen"
 * - Sichtbarkeit der InvoiceShowActivity
 */
@RunWith(AndroidJUnit4.class)
public class DisplayInvoiceShowActivityTest {

    /**
     * Startet die StartActivity vor jedem Test.
     */
    @Rule
    public ActivityScenarioRule<StartActivity> activityRule =
            new ActivityScenarioRule<>(StartActivity.class);

    /**
     * Setzt vor jedem Test eine Beispielrechnung in die globale InvoiceWrapper-Liste.
     */
    @Before
    public void setUp() {
        EateryInvoice invoice = new EateryInvoice();

        Address issuer = new Address("Name", "Street", 123456, "City", "Country");
        Item item = new Item("Item", 2, 0.07);

        invoice.setIssuer(issuer);
        invoice.setInvoiceID("INV123");
        invoice.setPaymentMethod("Bar");
        invoice.setDate(LocalDateTime.now());
        invoice.setVatID("VAT123");
        invoice.setTrnsactionID("TRANSACTION123");
        invoice.setCheckSum("CHECK123");
        invoice.setDeviceID("DEVICE123");
        invoice.addItem(item);

        InvoiceWrapper.INVOICES.clear();
        InvoiceWrapper.INVOICES.add(new InvoiceWrapper(invoice, "Bar"));
    }

    /**
     * Testet den kompletten Ablauf:
     * - Öffnet Rechnungshistorie
     * - Klickt Menü-Icon im ersten RecyclerView-Item
     * - Wählt im Popup-Menü "Anzeigen"
     * - Prüft, ob die Anzeige der Rechnung erfolgreich gestartet wurde
     *
     * @throws InterruptedException falls Thread.sleep() unterbrochen wird
     */
    @Test
    public void testShowSelectedInvoice() throws InterruptedException {
        // 1. Klick auf Button in StartActivity, um InvoiceHistoryActivity zu öffnen
        onView(withId(R.id.invoiceHistoryButton)).perform(click());

        // Kleine Wartezeit, bis RecyclerView geladen ist
        Thread.sleep(1000);

        // 2. Klick auf Menü-Icon im ersten RecyclerView-Item (scrollTo falls nötig)
        onView(withId(R.id.invoiceRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        clickChildViewWithId(R.id.invoiceMenuIcon)));

        // 3. Warte kurz, bis Popup erscheint
        Thread.sleep(500);

        // 4. Klick auf Popup-Menü-Eintrag "Anzeigen" im Popup (istPlatformPopup() wichtig!)
        onView(withText("Anzeigen"))
                .inRoot(isPlatformPopup())
                .perform(click());

        // 5. Prüfen, ob die InvoiceShowActivity angezeigt wird
        onView(withId(R.id.showInvoice))
                .check(matches(isDisplayed()));

    }

    /**
     * Hilfsmethode, um ein Kind-View in einem RecyclerView-Item per ID zu klicken.
     * Wird verwendet, um z.B. ein Menü-Icon innerhalb eines RecyclerView-Items anzuklicken.
     *
     * @param id ID des Kind-Views, der geklickt werden soll
     * @return ViewAction, der den Klick ausführt
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                // View muss sichtbar sein, damit geklickt werden kann
                return isDisplayed();
            }
            @Override
            public String getDescription() {
                return "Klicke auf Kind-View mit ID: " + id;
            }
            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }
}

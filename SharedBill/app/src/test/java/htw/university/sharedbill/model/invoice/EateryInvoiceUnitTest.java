package htw.university.sharedbill.model.invoice;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit-Tests für die Klasse {@link EateryInvoice}.
 * <p>
 * Testet folgende Aspekte:
 * <ul>
 *     <li>Hinzufügen und Entfernen von {@link Item}s</li>
 *     <li>Korrekte Preisberechnung (netto, brutto, Steuer)</li>
 *     <li>Gültigkeit der Rechnung</li>
 *     <li>Fehlerfälle (null-Werte, doppelte Entfernungen)</li>
 *     <li>Observer-Benachrichtigungen</li>
 *     <li>Map-Verhalten bei gleichen Items</li>
 * </ul>
 */
public class EateryInvoiceUnitTest {

    private EateryInvoice invoice;
    private Item item1;
    private Item item2;
    private Address issuer;

    /**
     * Initialisiert vor jedem Test eine neue Rechnung mit Testdaten.
     */
    @Before
    public void setup() {
        invoice = new EateryInvoice();
        issuer = new Address("Bäckerei Müller", "Hauptstr. 1", 12345, "Musterstadt", "Deutschland");
        item1 = new Item("Brötchen", "Frisch gebacken", 0.50, 0.07);
        item2 = new Item("Kaffee", "Groß", 2.00, 0.19);

        invoice.setIssuer(issuer);
        invoice.setInvoiceID("INV123");
        invoice.setPaymentMethod("Bar");
        invoice.setVatID("DE123456789");
        invoice.setTrnsactionID("TX987");
        invoice.setCheckSum("CHK001");
        invoice.setDeviceID("DEV42");
        invoice.setDate(LocalDateTime.now());
    }

    /**
     * Testet das Hinzufügen von Items.
     * Erwartet korrekte Aktualisierung von Artikelanzahl und Preiswerten.
     */
    @Test
    public void testAddItemUpdatesPrices() {
        invoice.addItem(item1);
        invoice.addItem(item2);
        invoice.addItem(item1); // zweites Brötchen hinzufügen

        Map<Item, Integer> items = invoice.getItems();
        assertEquals(2, items.size());
        assertEquals(2, (int) items.get(item1));
        assertEquals(1, (int) items.get(item2));

        double expectedNet = item1.getNetPrice() * 2 + item2.getNetPrice();
        double expectedGross = item1.getGrossPrice() * 2 + item2.getGrossPrice();
        double expectedTax = item1.getTaxPrice() * 2 + item2.getTaxPrice();

        assertEquals(expectedNet, invoice.getNetPrice(), 0.0001);
        assertEquals(expectedGross, invoice.getGrossPrice(), 0.0001);
        assertEquals(expectedTax, invoice.getTaxPrice(), 0.0001);
    }

    /**
     * Testet das Entfernen von Items.
     * Erwartet korrekte Reduktion der Mengen und Preise.
     */
    @Test
    public void testRemoveItemUpdatesPrices() {
        invoice.addItem(item1);
        invoice.addItem(item1);
        invoice.addItem(item2);

        invoice.removeItem(item1);

        Map<Item, Integer> items = invoice.getItems();
        assertEquals(2, items.size());
        assertEquals(1, (int) items.get(item1));
        assertEquals(1, (int) items.get(item2));

        double expectedNet = item1.getNetPrice() + item2.getNetPrice();
        assertEquals(expectedNet, invoice.getNetPrice(), 0.0001);
    }

    /**
     * Testet die Validierung einer Rechnung.
     * Erwartet: ungültig ohne Items, gültig mit mindestens einem Item.
     */
    @Test
    public void testIsValid() {
        assertFalse(invoice.isValid());

        invoice.addItem(item1);
        assertTrue(invoice.isValid());
    }

    /**
     * Testet das Hinzufügen eines {@code null}-Items.
     * Erwartet eine {@link IllegalArgumentException}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        invoice.addItem(null);
    }

    /**
     * Testet das Entfernen eines {@code null}-Items.
     * Erwartet eine {@link IllegalArgumentException}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullItem() {
        invoice.removeItem(null);
    }

    /**
     * Testet das Entfernen eines nicht vorhandenen Items.
     * Erwartet keine Änderungen und keinen Fehler.
     */
    @Test
    public void testRemoveNonExistingItem() {
        invoice.addItem(item1); // Nur item1 hinzugefügt
        invoice.removeItem(item2); // item2 wurde nie hinzugefügt

        assertEquals(1, (int) invoice.getItems().get(item1));
        assertFalse(invoice.getItems().containsKey(item2));
    }

    /**
     * Testet das Entfernen eines Items häufiger als es vorhanden ist.
     * Erwartet: Artikel ist nach erstem Entfernen weg, weitere Aufrufe verändern nichts.
     */
    @Test
    public void testRemoveItemMoreThanAdded() {
        invoice.addItem(item1);

        invoice.removeItem(item1);
        assertFalse(invoice.getItems().containsKey(item1));

        invoice.removeItem(item1); // zweiter Versuch, sollte ignoriert werden
        assertTrue(invoice.getItems().isEmpty());

        assertEquals(0.0, invoice.getNetPrice(), 0.0001);
        assertEquals(0.0, invoice.getGrossPrice(), 0.0001);
        assertEquals(0.0, invoice.getTaxPrice(), 0.0001);
    }

    /**
     * Testet das Hinzufügen von inhaltlich gleichen Items (gleiche Werte, aber andere Instanz).
     * Erwartet: Beide werden als identisch behandelt und gemeinsam gezählt.
     */
    @Test
    public void testAddEqualItems() {
        Item duplicate = new Item("Brötchen", "Frisch gebacken", 0.50, 0.07);
        invoice.addItem(item1);
        invoice.addItem(duplicate);

        assertEquals(1, invoice.getItems().size());
        assertEquals(2, (int) invoice.getItems().get(item1));
    }

    /**
     * Testet, ob ein Observer korrekt benachrichtigt wird, wenn sich der Zustand der Rechnung ändert.
     */
    @Test
    public void testObserverNotifiedOnAdd() {
        final boolean[] wasNotified = {false};

        InvoiceObserver observer = updatedInvoice -> wasNotified[0] = true;
        invoice.addObserver(observer);
        invoice.addItem(item1);

        assertTrue("Observer sollte benachrichtigt worden sein", wasNotified[0]);
    }

    /**
     * Testet das Entfernen eines Observers.
     * Erwartet, dass nach Entfernen keine Benachrichtigung mehr erfolgt.
     */
    @Test
    public void testRemoveObserver() {
        final boolean[] wasNotified = {false};
        InvoiceObserver observer = updatedInvoice -> wasNotified[0] = true;

        invoice.addObserver(observer);
        invoice.removeObserver(observer);
        invoice.addItem(item1);

        assertFalse("Observer sollte nicht benachrichtigt worden sein", wasNotified[0]);
    }
}

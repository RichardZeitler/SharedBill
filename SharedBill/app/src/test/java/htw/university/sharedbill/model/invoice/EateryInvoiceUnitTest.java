package htw.university.sharedbill.model.invoice;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

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
 *     <li>List-Verhalten bei gleichen Items</li>
 * </ul>
 */
public class EateryInvoiceUnitTest {

    private EateryInvoice invoice;
    private Item item1;
    private Item item2;
    private Address issuer;

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

    @Test
    public void testAddItemUpdatesPrices() {
        invoice.addItem(item1);
        invoice.addItem(item2);
        invoice.addItem(item2);

        List<Item> items = invoice.getItems();
        assertEquals(3, items.size());

        double expectedNet = item1.getNetPrice() + item2.getNetPrice() * 2;
        double expectedGross = item1.getGrossPrice() + item2.getGrossPrice() * 2;
        double expectedTax = item1.getTaxPrice() + item2.getTaxPrice() * 2;

        assertEquals(expectedNet, invoice.getNetPrice(), 0.0001);
        assertEquals(expectedGross, invoice.getGrossPrice(), 0.0001);
        assertEquals(expectedTax, invoice.getTaxPrice(), 0.0001);
    }

    @Test
    public void testRemoveItemUpdatesPrices() {
        invoice.addItem(item1);
        invoice.addItem(item1);
        invoice.addItem(item2);

        invoice.removeItem(item1);

        List<Item> items = invoice.getItems();
        assertEquals(2, items.size());

        double expectedNet = item1.getNetPrice() + item2.getNetPrice();
        assertEquals(expectedNet, invoice.getNetPrice(), 0.0001);
    }

    @Test
    public void testIsValid() {
        assertFalse(invoice.isValid());

        invoice.addItem(item1);
        assertTrue(invoice.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        invoice.addItem(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullItem() {
        invoice.removeItem(null);
    }

    @Test
    public void testRemoveNonExistingItem() {
        invoice.addItem(item1); // Nur item1 hinzugefügt
        invoice.removeItem(item2); // item2 wurde nie hinzugefügt

        List<Item> items = invoice.getItems();
        assertEquals(1, items.size());
        assertTrue(items.contains(item1));
        assertFalse(items.contains(item2));
    }

    @Test
    public void testRemoveItemMoreThanAdded() {
        invoice.addItem(item1);

        invoice.removeItem(item1);
        List<Item> items = invoice.getItems();
        assertFalse(items.contains(item1));

        invoice.removeItem(item1); // zweiter Versuch, sollte ignoriert werden
        assertTrue(invoice.getItems().isEmpty());

        assertEquals(0.0, invoice.getNetPrice(), 0.0001);
        assertEquals(0.0, invoice.getGrossPrice(), 0.0001);
        assertEquals(0.0, invoice.getTaxPrice(), 0.0001);
    }

    @Test
    public void testAddEqualItems() {
        Item duplicate = new Item("Brötchen", "Frisch gebacken", 0.50, 0.07);
        invoice.addItem(item1);
        invoice.addItem(duplicate);

        List<Item> items = invoice.getItems();
        assertEquals(2, items.size());

        long frequency = items.stream().filter(i -> i.equals(item1)).count();
        assertEquals(2, frequency);
    }

    @Test
    public void testObserverNotifiedOnAdd() {
        final boolean[] wasNotified = {false};

        InvoiceObserver observer = updatedInvoice -> wasNotified[0] = true;
        invoice.addObserver(observer);
        invoice.addItem(item1);

        assertTrue("Observer sollte benachrichtigt worden sein", wasNotified[0]);
    }

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

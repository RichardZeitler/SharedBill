package htw.university.sharedbill.model.invoice;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit-Tests für die Klasse {@link Address}.
 * Diese Tests überprüfen die korrekte Erstellung, Gleichheit und Validierung von {@link Address}-Objekten.
 */
public class AddressUnitTest {

    /**
     * Testet die korrekte Erstellung einer {@link Address} mit gültigen Werten.
     * Erwartet, dass alle Felder korrekt gesetzt werden.
     */
    @Test
    public void testNormal() {
        Address address = new Address("Max Mustermann", "Musterstraße 1", 12345, "Musterstadt", "Deutschland");

        assertEquals("Max Mustermann", address.getName());
        assertEquals("Musterstraße 1", address.getStreet());
        assertEquals(12345, address.getZip());
        assertEquals("Musterstadt", address.getCity());
        assertEquals("Deutschland", address.getCountry());
    }

    /**
     * Testet die Methoden {@code equals()} von {@link Address}.
     * Überprüft, dass gleiche Adressen als gleich erkannt werden und unterschiedliche Adressen nicht.
     */
    @Test
    public void testEqualsAddress() {
        Address a1 = new Address("Max Mustermann", "Musterstraße 1", 12345, "Musterstadt", "Deutschland");
        Address a2 = new Address("Max Mustermann", "Musterstraße 1", 12345, "Musterstadt", "Deutschland");
        Address a3 = new Address("Anna Beispiel", "Andere Straße 2", 54321, "Beispielstadt", "Österreich");

        assertEquals(a1, a2);
        assertNotEquals(a1, a3);
        assertNotEquals(a2, a3);
    }

    /**
     * Testet, dass die Erstellung einer {@link Address} mit leerem Namen (leerer String oder null)
     * eine {@link IllegalArgumentException} auslöst.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidName() {
        new Address("", "Musterstraße 1", 12345, "Musterstadt", "Deutschland");
        new Address(null, "Musterstraße 1", 12345, "Musterstadt", "Deutschland");
    }

    /**
     * Testet, dass die Erstellung einer {@link Address} mit leerer Straße (leerer String oder null)
     * eine {@link IllegalArgumentException} auslöst.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidStreet() {
        new Address("Max Mustermann", "", 12345, "Musterstadt", "Deutschland");
        new Address("Max Mustermann", null, 12345, "Musterstadt", "Deutschland");
    }

    /**
     * Testet, dass die Erstellung einer {@link Address} mit leerer Stadt (leerer String oder null)
     * eine {@link IllegalArgumentException} auslöst.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCity() {
        new Address("Max Mustermann", "Musterstraße 1", 12345, "", "Deutschland");
        new Address("Max Mustermann", "Musterstraße 1", 12345, null, "Deutschland");
    }

    /**
     * Testet, dass die Erstellung einer {@link Address} mit leerem Land (leerer String oder null)
     * eine {@link IllegalArgumentException} auslöst.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCountry() {
        new Address("Max Mustermann", "Musterstraße 1", 12345, "Musterstadt", "");
        new Address("Max Mustermann", "Musterstraße 1", 12345, "Musterstadt", null);
    }

    /**
     * Testet, dass die Erstellung einer {@link Address} mit negativer Postleitzahl
     * eine {@link IllegalArgumentException} auslöst.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidZip() {
        new Address("Max Mustermann", "Musterstraße 1", -1, "Musterstadt", "Deutschland");
    }
}

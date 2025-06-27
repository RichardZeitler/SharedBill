package htw.university.sharedbill.model.invoice;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit-Tests für die Klasse {@link Item}.
 * Diese Tests überprüfen die korrekte Initialisierung, Validierung und Vergleichsfunktionalität von {@link Item}-Objekten.
 */
public class ItemUnitTest {

    /**
     * Testet die Erstellung eines {@link Item} ohne explizite Beschreibung.
     * Erwartet, dass die Beschreibung automatisch auf "keine" gesetzt wird
     * und die Preise sowie der Steuersatz korrekt initialisiert werden.
     */
    @Test
    public void testNormalNoDescription() {
        Item item = new Item("Wasser", 1.19, 0.19);
        assertEquals("Wasser", item.getName());
        assertEquals("keine", item.getDescription());
        assertEquals(1.19, item.getGrossPrice(), 0.001);
        assertEquals(0.19, item.getTaxRate(), 0.001);
        assertEquals(1.0, item.getNetPrice(), 0.001);
        assertEquals(1.19 - item.getNetPrice(), item.getTaxPrice(), 0.001);
    }

    /**
     * Testet die Erstellung eines {@link Item} mit einer expliziten Beschreibung.
     * Erwartet, dass Name und Beschreibung korrekt gespeichert werden.
     */
    @Test
    public void testNormalWithDescription() {
        Item item = new Item("Apfel", "Granny Smith", 2.38, 0.19);
        assertEquals("Apfel", item.getName());
        assertEquals("Granny Smith", item.getDescription());
    }

    /**
     * Testet, dass bei leerem Namen (leerer String oder null) im Konstruktor eine {@link IllegalArgumentException} geworfen wird.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidName() {
        new Item("", 1.00, 0.19);
        new Item(null, 1.00, 0.19);
    }

    /**
     * Testet, dass ein negativer Bruttopreis im Konstruktor eine {@link IllegalArgumentException} auslöst.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeGrossPrice() {
        new Item("Brot", -5.0, 0.19);
    }

    /**
     * Testet die {@code equals()}-Methode der Klasse {@link Item}.
     * Zwei Items mit gleichen Attributen sollen als gleich erkannt werden,
     * Items mit unterschiedlicher Beschreibung jedoch nicht.
     */
    @Test
    public void testEquals() {
        Item a = new Item("Milch", "1L", 1.00, 0.19);
        Item b = new Item("Milch", "1L", 1.00, 0.19);
        Item c = new Item("Milch", "2L", 1.00, 0.19);

        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    /**
     * Testet die automatische Umrechnung des Steuersatzes,
     * falls dieser als Prozentzahl (z.B. 7) statt als Dezimalzahl angegeben wird.
     * Erwartet, dass der Wert intern korrekt als Dezimal (0.07) gespeichert wird.
     */
    @Test
    public void testTaxRateConversionFromPercent() {
        Item item = new Item("TestItem", "TestDesc", 10.0, 7); // 7% -> 0.07 intern
        assertEquals(0.07, item.getTaxRate(), 0.0001);
    }

    /**
     * Testet, dass ein negativer Steuersatz im Konstruktor eine {@link IllegalArgumentException} auslöst.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeTaxRate() {
        new Item("Kaffee", "Arabica", 3.50, -0.07);
    }

}

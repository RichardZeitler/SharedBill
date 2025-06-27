package htw.university.sharedbill.model.invoice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

import java.time.LocalDateTime;

@RunWith(RobolectricTestRunner.class)
public class JSONTest {

    private EateryInvoice invoice;
    private Item item1;
    private Item item2;
    private Address issuer;

    /**
     * Setzt vor jedem Test eine Beispielrechnung mit Items und einem Issuer (Aussteller) auf.
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
     * Erstellt ein Test-JSON-Objekt für ein Item mit allen relevanten Feldern.
     *
     * @param name        Name des Items
     * @param description Beschreibung des Items
     * @param net         Nettopreis
     * @param taxRate     Steuersatz
     * @param tax         Steuerbetrag
     * @param gross       Bruttopreis
     * @return JSONObject mit den Item-Daten
     * @throws JSONException falls JSON-Fehler auftreten
     */
    private JSONObject createTestItemJson(String name, String description, double net, double taxRate, double tax, double gross) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("description", description);
        json.put("netPrice", net);
        json.put("taxRate", taxRate);
        json.put("taxPrice", tax);
        json.put("grossPrice", gross);
        return json;
    }

    /**
     * Erstellt ein Test-JSON-Objekt für eine {@link Address}.
     *
     * @return JSONObject mit den Address-Daten
     * @throws JSONException falls JSON-Fehler auftreten
     */
    private JSONObject createTestIssuerJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", "Bäckerei Müller");
        json.put("street", "Hauptstr. 1");
        json.put("zip", 12345);
        json.put("city", "Musterstadt");
        json.put("country", "Deutschland");
        return json;
    }

    /**
     * Testet die korrekte Serialisierung eines {@link Item}-Objekts in ein {@link JSONObject}.
     *
     * @throws JSONException falls JSON-Fehler auftreten
     */
    @Test
    public void testJsonItemSerialization() throws JSONException {
        Item item = new Item("Test", 1.19, 0.19);
        JSONObject json = item.getJSONObject();
        assertEquals("Test", json.getString("name"));
        assertEquals(1.0, json.getDouble("netPrice"), 0.001);
        assertEquals(0.19, json.getDouble("taxRate"), 0.001);
        assertEquals(1.19, json.getDouble("grossPrice"), 0.001);
    }

    /**
     * Testet die Deserialisierung eines {@link Item} aus einem gültigen {@link JSONObject}.
     *
     * @throws JSONException falls JSON-Fehler auftreten
     */
    @Test
    public void testJsonItemDeserialization() throws JSONException {
        JSONObject json = createTestItemJson("TestItem", "TestDescription", 1.0, 0.19, 0.19, 1.19);
        Item item = Item.fromJSONObject(json);

        assertEquals("TestItem", item.getName());
        assertEquals("TestDescription", item.getDescription());
        assertEquals(1.19, item.getGrossPrice(), 0.001);
        assertEquals(1.0, item.getNetPrice(), 0.001);
        assertEquals(0.19, item.getTaxRate(), 0.001);
        assertEquals(0.19, item.getTaxPrice(), 0.001);
    }

    /**
     * Testet, ob eine {@link JSONException} geworfen wird, wenn beim Deserialisieren eines {@link Item}
     * das Pflichtfeld "name" fehlt.
     *
     * @throws JSONException erwartet, da "name" im JSON nicht enthalten ist
     */
    @Test(expected = JSONException.class)
    public void testJsonItemDeserializationWithMissingField() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("description", "TestDescription");
        json.put("grossPrice", 1.19);
        json.put("taxRate", 0.19);

        Item.fromJSONObject(json);
    }

    /**
     * Testet die Serialisierung eines {@link Address}-Objekts in ein {@link JSONObject}.
     *
     * @throws JSONException falls JSON-Fehler auftreten
     */
    @Test
    public void testJsonAddressSerialization() throws JSONException {
        Address address = new Address("Max Mustermann", "Musterstraße 1", 12345, "Musterstadt", "Deutschland");
        JSONObject json = address.getJSONObject();

        assertEquals("Max Mustermann", json.getString("name"));
        assertEquals("Musterstraße 1", json.getString("street"));
        assertEquals(12345, json.getInt("zip"));
        assertEquals("Musterstadt", json.getString("city"));
        assertEquals("Deutschland", json.getString("country"));
    }

    /**
     * Testet die Deserialisierung eines {@link JSONObject} in ein {@link Address}-Objekt.
     *
     * @throws JSONException falls JSON-Fehler auftreten
     */
    @Test
    public void testJsonAddressDeserialization() throws JSONException {
        JSONObject json = createTestIssuerJson();
        Address address = Address.fromJSONObject(json);

        assertEquals("Bäckerei Müller", address.getName());
        assertEquals("Hauptstr. 1", address.getStreet());
        assertEquals(12345, address.getZip());
        assertEquals("Musterstadt", address.getCity());
        assertEquals("Deutschland", address.getCountry());
    }

    /**
     * Testet, ob eine {@link JSONException} geworfen wird, wenn beim Deserialisieren einer {@link Address}
     * das Pflichtfeld "name" fehlt.
     *
     * @throws JSONException erwartet, da das Feld "name" nicht vorhanden ist
     */
    @Test(expected = JSONException.class)
    public void testJsonAddressDeserializationWithMissingField() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("test", "Max Mustermann");
        json.put("street", "Musterstraße 1");
        json.put("zip", 12345);

        Address.fromJSONObject(json);
    }

    /**
     * Testet die Serialisierung einer vollständigen {@link EateryInvoice} inklusive Items und Issuer.
     *
     * @throws JSONException falls JSON-Fehler auftreten
     */
    @Test
    public void testInvoiceSerialization() throws JSONException {
        invoice.addItem(item1);
        invoice.addItem(item2);

        JSONObject json = invoice.getJSONObject();

        assertEquals("DE123456789", json.get("vatID"));
        assertEquals(2.1479619885337313, json.get("netPrice"));
        assertEquals(0.35203801146626873, json.get("taxPrice"));
        assertEquals("Bar", json.get("paymentMethod"));
        assertEquals("CHK001", json.get("checkSum"));
        assertEquals("INV123", json.get("invoiceID"));
        assertEquals(2.5, json.get("grossPrice"));
        assertEquals("DEV42", json.get("deviceID"));
        assertEquals("TX987", json.get("transactionID"));

        JSONObject issuerJson = json.getJSONObject("issuer");
        assertEquals("Bäckerei Müller", issuerJson.getString("name"));
        assertEquals("Hauptstr. 1", issuerJson.getString("street"));
        assertEquals(12345, issuerJson.getInt("zip"));
        assertEquals("Musterstadt", issuerJson.getString("city"));

        JSONArray itemsArray = json.getJSONArray("items");
        assertEquals(2, itemsArray.length());

        JSONObject firstItem = itemsArray.getJSONObject(0);
        assertEquals("Brötchen", firstItem.getString("name"));
        assertEquals("Frisch gebacken", firstItem.getString("description"));
        assertEquals(0.4672897196261682, firstItem.getDouble("netPrice"), 0.000001);
        assertEquals(0.03271028037383178, firstItem.getDouble("taxPrice"), 0.000001);
        assertEquals(0.5, firstItem.getDouble("grossPrice"), 0.000001);
        assertEquals(0.07, firstItem.getDouble("taxRate"), 0.000001);

        JSONObject secondItem = itemsArray.getJSONObject(1);
        assertEquals("Kaffee", secondItem.getString("name"));
        assertEquals("Groß", secondItem.getString("description"));
        assertEquals(1.680672268907563, secondItem.getDouble("netPrice"), 0.000001);
        assertEquals(0.31932773109243695, secondItem.getDouble("taxPrice"), 0.000001);
        assertEquals(2, secondItem.getDouble("grossPrice"), 0.000001);
        assertEquals(0.19, secondItem.getDouble("taxRate"), 0.000001);
    }

    /**
     * Testet die Deserialisierung einer vollständigen {@link EateryInvoice} aus einem {@link JSONObject}.
     *
     * @throws JSONException falls JSON-Fehler auftreten
     */
    @Test
    public void testInvoiceDeserialization() throws JSONException {
        JSONObject invoiceJson = new JSONObject();
        invoiceJson.put("date", "2025-06-26T19:33:04.772817");
        invoiceJson.put("vatID", "DE123456789");
        invoiceJson.put("netPrice", 2.1479619885337313);
        invoiceJson.put("taxPrice", 0.35203801146626873);
        invoiceJson.put("paymentMethod", "Bar");
        invoiceJson.put("checkSum", "CHK001");
        invoiceJson.put("invoiceID", "INV123");
        invoiceJson.put("grossPrice", 2.5);
        invoiceJson.put("deviceID", "DEV42");
        invoiceJson.put("transactionID", "TX987");

        JSONObject coffeeJson = createTestItemJson("Kaffee", "Groß", 1.680672268907563, 0.19, 0.31932773109243695, 2.0);
        coffeeJson.put("quantity", 1);

        JSONObject broetchenJson = createTestItemJson("Brötchen", "Frisch gebacken", 0.4672897196261682, 0.07, 0.03271028037383178, 0.5);
        broetchenJson.put("quantity", 1);

        JSONArray items = new JSONArray();
        items.put(coffeeJson);
        items.put(broetchenJson);
        invoiceJson.put("items", items);

        JSONObject issuerJson = createTestIssuerJson();
        invoiceJson.put("issuer", issuerJson);

        EateryInvoice inv = EateryInvoice.fromJSONObject(invoiceJson);
        assertTrue(inv.isValid());
    }

    /**
     * Testet, ob beim Deserialisieren einer fehlerhaften {@link EateryInvoice} eine {@link JSONException}
     * geworfen wird, wenn notwendige Felder fehlen.
     *
     * @throws JSONException erwartet, da wichtige Felder wie "issuer" oder "items" fehlen
     */
    @Test(expected = JSONException.class)
    public void testJsonInvoiceDeserializationWithMissingField() throws JSONException {
        JSONObject invoiceJson = new JSONObject();
        invoiceJson.put("date", "2025-06-26T19:33:04.772817");
        invoiceJson.put("vatID", "DE123456789");
        invoiceJson.put("netPrice", 2.1479619885337313);
        invoiceJson.put("taxPrice", 0.35203801146626873);
        invoiceJson.put("paymentMethod", "Bar");
        invoiceJson.put("checkSum", "CHK001");
        invoiceJson.put("invoiceID", "INV123");
        invoiceJson.put("grossPrice", 2.5);
        invoiceJson.put("deviceID", "DEV42");
        invoiceJson.put("transactionID", "TX987");

        // Keine items und issuer gesetzt - muss JSONException auslösen
        EateryInvoice.fromJSONObject(invoiceJson);
    }
}

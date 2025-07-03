package htw.university.sharedbill.model.invoice;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Schnittstelle für eine Rechnung (Invoice).
 * Definiert die grundlegenden Methoden zum Zugriff auf Rechnungsdaten
 * sowie zum Hinzufügen und Entfernen von Rechnungspositionen (Items).
 */
public interface Invoice {

    /**
     * Liefert den Aussteller der Rechnung.
     *
     * @return Adresse des Ausstellers.
     */
    Address getIssuer();

    /**
     * Liefert die eindeutige Rechnungs-ID.
     *
     * @return Rechnungs-ID als String.
     */
    String getInvoiceID();

    /**
     * Liefert die Liste der Rechnungspositionen.
     *
     * @return Liste von Items.
     */
    List<Item> getItems();

    /**
     * Liefert den Nettobetrag der Rechnung (ohne Steuern).
     *
     * @return Nettobetrag als double.
     */
    double getNetPrice();

    /**
     * Liefert den Bruttobetrag der Rechnung (inkl. Steuern).
     *
     * @return Bruttobetrag als double.
     */
    double getGrossPrice();

    /**
     * Liefert den Steuerbetrag der Rechnung.
     *
     * @return Steuerbetrag als double.
     */
    double getTaxPrice();

    /**
     * Liefert die Zahlungsart der Rechnung.
     *
     * @return Zahlungsart als String.
     */
    String getPaymentMethod();

    /**
     * Liefert das Datum der Rechnung.
     *
     * @return Datum als LocalDateTime.
     */
    LocalDateTime getDate();

    /**
     * Liefert die Umsatzsteuer-ID.
     *
     * @return Umsatzsteuer-ID als String.
     */
    String getVatID();

    /**
     * Liefert die Transaktions-ID.
     *
     * @return Transaktions-ID als String.
     */
    String getTransactionID();

    /**
     * Liefert die Prüfsumme der Rechnung.
     *
     * @return Prüfsumme als String.
     */
    String getCheckSum();

    /**
     * Fügt der Rechnung eine neue Position hinzu.
     *
     * @param item Hinzuzufügendes Item.
     */
    void addItem(Item item);

    /**
     * Entfernt eine Position aus der Rechnung.
     *
     * @param item Zu entfernendes Item.
     */
    void removeItem(Item item);

    /**
     * Wandelt die Rechnung in ein JSON-Objekt um.
     *
     * @return JSONObject mit Rechnungsdaten.
     * @throws JSONException falls ein Fehler beim Erstellen des JSON auftritt.
     */
    JSONObject getJSONObject() throws JSONException;

    /**
     * Erstellt eine Rechnung aus einem JSON-Objekt.
     * Implementierung hängt von der konkreten Klasse ab (hier EateryInvoice).
     *
     * @param json JSON-Objekt mit Rechnungsdaten.
     * @return Neue Invoice-Instanz.
     * @throws JSONException falls ein Fehler beim Auslesen des JSON auftritt.
     */
    static Invoice fromJSONObject(JSONObject json) throws JSONException {
        return EateryInvoice.fromJSONObject(json);
    }
}

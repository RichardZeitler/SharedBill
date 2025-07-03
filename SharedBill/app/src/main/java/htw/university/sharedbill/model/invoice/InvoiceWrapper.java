package htw.university.sharedbill.model.invoice;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper-Klasse für eine {@link Invoice}, die zusätzliche Informationen
 * wie den Zahlungsstatus enthält. Implementiert {@link Serializable} zur
 * Objekt-Serialisierung.
 */
public class InvoiceWrapper implements Serializable {

    /**
     * Der Zahlungsstatus der Rechnung (z.B. "bezahlt", "offen").
     */
    private String paymentStatus;

    /**
     * Die eingewickelte Rechnung.
     */
    private Invoice invoice;

    /**
     * Eine globale Liste, die alle in dieser Klasse eingewickelten Rechnungen enthält.
     */
    public static List<InvoiceWrapper> INVOICES = new ArrayList<>();

    /**
     * Erzeugt eine neue {@code InvoiceWrapper}-Instanz mit der übergebenen Rechnung
     * und dem Zahlungsstatus.
     *
     * @param invoice       die Rechnung, die eingewickelt werden soll; darf nicht null sein
     * @param paymentStatus der Zahlungsstatus; darf nicht null oder leer sein
     * @throws IllegalArgumentException falls invoice null ist oder paymentStatus null/leer ist
     */
    public InvoiceWrapper(Invoice invoice, String paymentStatus) {
        setInvoice(invoice);
        setPaymentStatus(paymentStatus);
    }

    /**
     * Setzt die Rechnung.
     *
     * @param invoice die Rechnung; darf nicht null sein
     * @throws IllegalArgumentException falls invoice null ist
     */
    private void setInvoice(Invoice invoice) {
        if (invoice == null) throw new IllegalArgumentException("[InvoiceWrapper] Rechnung ist ungültig.");
        this.invoice = invoice;
    }

    /**
     * Setzt den Zahlungsstatus.
     *
     * @param paymentStatus der Zahlungsstatus; darf nicht null oder leer sein
     * @throws IllegalArgumentException falls paymentStatus null oder leer ist
     */
    public void setPaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isEmpty())
            throw new IllegalArgumentException("[InvoiceWrapper] Rechnungsstatus ist ungültig.");
        this.paymentStatus = paymentStatus;
    }

    /**
     * Gibt die eingewickelte Rechnung zurück.
     *
     * @return die Rechnung
     */
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Gibt den Zahlungsstatus zurück.
     *
     * @return der Zahlungsstatus
     */
    public String getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * Vergleicht dieses Objekt mit einem anderen auf Gleichheit basierend auf der Rechnungs-ID.
     *
     * @param obj das zu vergleichende Objekt
     * @return true, falls die Rechnungs-IDs gleich sind, sonst false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        InvoiceWrapper that = (InvoiceWrapper) obj;
        return invoice != null && that.invoice != null &&
                Objects.equals(invoice.getInvoiceID(), that.invoice.getInvoiceID());
    }

    /**
     * Gibt den Hash-Code basierend auf der Rechnungs-ID zurück.
     *
     * @return der Hash-Code der Rechnungs-ID oder 0, falls invoice null ist
     */
    @Override
    public int hashCode() {
        return invoice != null ? invoice.getInvoiceID().hashCode() : 0;
    }

    /**
     * Serialisiert diesen {@code InvoiceWrapper} als JSON-Objekt.
     *
     * @return ein {@link JSONObject} mit der Repräsentation dieses Wrappers
     * @throws JSONException falls ein Fehler bei der JSON-Erstellung auftritt
     */
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("paymentStatus", paymentStatus);
        json.put("invoice", invoice.getJSONObject());

        return json;
    }

    /**
     * Erzeugt einen {@code InvoiceWrapper} aus einem JSON-Objekt.
     *
     * @param json das JSON-Objekt mit den Daten des InvoiceWrappers
     * @return eine neue {@code InvoiceWrapper}-Instanz
     * @throws JSONException falls ein Fehler beim Parsen des JSON auftritt
     */
    public static InvoiceWrapper fromJSONObject(JSONObject json) throws JSONException {
        String status = json.getString("paymentStatus");
        JSONObject invoiceJson = json.getJSONObject("invoice");
        Invoice inv = Invoice.fromJSONObject(invoiceJson);
        return new InvoiceWrapper(inv, status);
    }
}

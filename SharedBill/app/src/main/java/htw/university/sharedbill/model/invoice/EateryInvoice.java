package htw.university.sharedbill.model.invoice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import htw.university.sharedbill.model.Utils;

/**
 * Repräsentiert eine Rechnung für eine Gaststätte (EateryInvoice).
 * Diese Klasse implementiert das {@link Invoice} Interface und ist serialisierbar.
 * Sie verwaltet Informationen über Rechnungssteller, Rechnungsposten, Preise und weitere Metadaten.
 */
public class EateryInvoice implements Invoice, Serializable {
    private Address issuer;
    private String invoiceID;
    private double netPrice;
    private double grossPrice;
    private double taxPrice;
    private String paymentMethod;
    private LocalDateTime date;
    private String vatID;
    private String trnsactionID;
    private String checkSum;
    private String deviceID;

    private List<Item> items = new ArrayList<>();
    private transient List<InvoiceObserver> invoiceObserverList = new ArrayList<>();

    /**
     * Fügt einen Beobachter hinzu, der über Änderungen an der Rechnung informiert wird.
     *
     * @param invoiceObserver der hinzuzufügende Beobachter
     */
    public void addObserver(InvoiceObserver invoiceObserver) {
        if (invoiceObserver != null && !invoiceObserverList.contains(invoiceObserver))
            invoiceObserverList.add(invoiceObserver);
    }

    /**
     * Entfernt einen Beobachter von der Liste der Beobachter.
     *
     * @param invoiceObserver der zu entfernende Beobachter
     */
    public void removeObserver(InvoiceObserver invoiceObserver) {
        invoiceObserverList.remove(invoiceObserver);
    }

    /**
     * Benachrichtigt alle registrierten Beobachter über eine Änderung an der Rechnung.
     */
    protected void notifyObserver() {
        invoiceObserverList.forEach(invoiceObserver -> invoiceObserver.updateInvoice(this));
    }

    @Override
    public Address getIssuer() {
        return issuer;
    }

    @Override
    public String getInvoiceID() {
        return invoiceID;
    }

    @Override
    public List<Item> getItems() {
        return items;
    }

    @Override
    public double getNetPrice() {
        return netPrice;
    }

    @Override
    public double getGrossPrice() {
        return grossPrice;
    }

    @Override
    public double getTaxPrice() {
        return taxPrice;
    }

    @Override
    public String getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String getVatID() {
        return vatID;
    }

    @Override
    public String getTransactionID() {
        return trnsactionID;
    }

    @Override
    public String getCheckSum() {
        return checkSum;
    }

    /**
     * Fügt der Rechnung einen neuen Artikel hinzu und aktualisiert die Preise.
     *
     * @param item der hinzuzufügende Artikel
     * @throws IllegalArgumentException wenn der Artikel null ist
     */
    @Override
    public void addItem(Item item) {
        if (item == null) throw new IllegalArgumentException("[EateryInvoice] Item zum Hinzufügen ist ungültig.");
        items.add(item);

        setNetPrice(netPrice + item.getNetPrice());
        setGrossPrice(grossPrice + item.getGrossPrice());
        setTaxPrice(taxPrice + item.getTaxPrice());

        notifyObserver();
    }

    /**
     * Entfernt einen Artikel aus der Rechnung und aktualisiert die Preise.
     *
     * @param item der zu entfernende Artikel
     * @throws IllegalArgumentException wenn der Artikel null ist
     */
    @Override
    public void removeItem(Item item) {
        if (item == null) throw new IllegalArgumentException("[EateryInvoice] Item zum Entfernen ist ungültig.");
        if (items.remove(item)) {
            setNetPrice(netPrice - item.getNetPrice());
            setGrossPrice(grossPrice - item.getGrossPrice());
            setTaxPrice(taxPrice - item.getTaxPrice());

            notifyObserver();
        }
    }

    /**
     * Gibt die Geräte-ID zurück, von der die Rechnung stammt.
     *
     * @return Geräte-ID als String
     */
    public String getDeviceID() {
        return deviceID;
    }

    private void setNetPrice(double netPrice) {
        if (!Double.isFinite(netPrice) || netPrice < 0) throw new IllegalArgumentException("[EateryInvoice] Nettopreis ist ungültig.");
        this.netPrice = Utils.roundToDecimals(netPrice, 2);
    }

    private void setTaxPrice(double taxPrice) {
        if (!Double.isFinite(taxPrice) || taxPrice < 0) throw new IllegalArgumentException("[EateryInvoice] Steuerbetrag ist ungültig.");
        this.taxPrice = Utils.roundToDecimals(taxPrice,2);
    }

    private void setGrossPrice(double grossPrice) {
        if (!Double.isFinite(grossPrice) || grossPrice < 0) throw new IllegalArgumentException("[EateryInvoice] Bruttobetrag ist ungültig.");
        this.grossPrice = Utils.roundToDecimals(grossPrice, 2);
    }

    /**
     * Setzt die Zahlungsmethode der Rechnung.
     *
     * @param paymentMethod Zahlungsmethode als String
     * @throws IllegalArgumentException wenn die Zahlungsmethode null oder leer ist
     */
    public void setPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Zahlungsmethode ist ungültig.");
        this.paymentMethod = paymentMethod;
    }

    /**
     * Setzt den Rechnungsaussteller.
     *
     * @param issuer Adresse des Ausstellers
     * @throws IllegalArgumentException wenn der Aussteller null ist
     */
    public void setIssuer(Address issuer) {
        if (issuer == null) throw new IllegalArgumentException("[EateryInvoice] Rechnungsaussteller ist ungültig.");
        this.issuer = issuer;
    }

    /**
     * Setzt die Checksumme der Rechnung.
     *
     * @param checkSum Checksumme als String
     * @throws IllegalArgumentException wenn die Checksumme null oder leer ist
     */
    public void setCheckSum(String checkSum) {
        if (checkSum == null || checkSum.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Checksumme ist ungültig.");
        this.checkSum = checkSum;
    }

    /**
     * Setzt das Ausstellungsdatum der Rechnung.
     *
     * @param date Datum der Ausstellung
     * @throws IllegalArgumentException wenn das Datum null ist
     */
    public void setDate(LocalDateTime date) {
        if (date == null) throw new IllegalArgumentException("[EateryInvoice] Ausstellungsdatum ist ungültig.");
        this.date = date;
    }

    /**
     * Setzt die Geräte-ID der Rechnung.
     *
     * @param deviceID Geräte-ID als String
     * @throws IllegalArgumentException wenn die Geräte-ID null oder leer ist
     */
    public void setDeviceID(String deviceID) {
        if (deviceID == null || deviceID.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Geräteid ist ungültig.");
        this.deviceID = deviceID;
    }

    /**
     * Setzt die Rechnungs-ID.
     *
     * @param invoiceID ID der Rechnung als String
     * @throws IllegalArgumentException wenn die ID null oder leer ist
     */
    public void setInvoiceID(String invoiceID) {
        if (invoiceID == null || invoiceID.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Rechnungsid ist ungültig.");
        this.invoiceID = invoiceID;
    }

    /**
     * Setzt die Transaktions-ID.
     *
     * @param trnsactionID Transaktions-ID als String
     * @throws IllegalArgumentException wenn die ID null oder leer ist
     */
    public void setTrnsactionID(String trnsactionID) {
        if (trnsactionID == null || trnsactionID.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Transaktionsid ist ungültig.");
        this.trnsactionID = trnsactionID;
    }

    /**
     * Setzt die Umsatzsteuer-ID (VAT ID).
     *
     * @param vatID Umsatzsteuer-ID als String
     * @throws IllegalArgumentException wenn die VAT ID null oder leer ist
     */
    public void setVatID(String vatID) {
        if (vatID == null || vatID.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Steueridentifikationsnummer ist ungültig.");
        this.vatID = vatID;
    }

    /**
     * Prüft, ob die Rechnung vollständig und gültig ist.
     *
     * @return true, wenn alle erforderlichen Felder gültig gesetzt sind, sonst false
     */
    public boolean isValid() {
        return issuer != null &&
                invoiceID != null && !invoiceID.isEmpty() &&
                Double.isFinite(netPrice) && netPrice >= 0 &&
                Double.isFinite(grossPrice) && grossPrice >= 0 &&
                Double.isFinite(taxPrice) && taxPrice >= 0 &&
                paymentMethod != null && !paymentMethod.isEmpty() &&
                vatID != null && !vatID.isEmpty() &&
                trnsactionID != null && !trnsactionID.isEmpty() &&
                checkSum != null && !checkSum.isEmpty() &&
                deviceID != null && !deviceID.isEmpty() &&
                date != null &&
                items != null && !items.isEmpty();
    }

    /**
     * Serialisiert das Objekt, insbesondere das Datum als String.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(date.toString());
    }

    /**
     * Deserialisiert das Objekt und parst das Datum aus dem String.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        String creationDateString = (String) in.readObject();
        this.date = LocalDateTime.parse(creationDateString);
    }

    /**
     * Konvertiert die Rechnung in ein JSON-Objekt.
     *
     * @return JSONObject mit allen Rechnungsdaten
     * @throws JSONException bei JSON-Fehlern
     */
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("invoiceID", invoiceID);
        json.put("netPrice", netPrice);
        json.put("grossPrice", grossPrice);
        json.put("taxPrice", taxPrice);
        json.put("paymentMethod", paymentMethod);
        json.put("date", date.toString());
        json.put("vatID", vatID);
        json.put("transactionID", trnsactionID);
        json.put("checkSum", checkSum);
        json.put("deviceID", deviceID);
        json.put("issuer", issuer.getJSONObject());

        JSONArray itemsArray = new JSONArray();
        for (Item item : items) {
            JSONObject itemJson = item.getJSONObject();
            itemsArray.put(itemJson);
        }

        json.put("items", itemsArray);

        return json;
    }

    /**
     * Erzeugt eine EateryInvoice-Instanz aus einem JSON-Objekt.
     *
     * @param json JSONObject mit Rechnungsdaten
     * @return EateryInvoice-Objekt
     * @throws JSONException bei JSON-Parsing-Fehlern
     */
    public static EateryInvoice fromJSONObject(JSONObject json) throws JSONException {
        EateryInvoice invoice = new EateryInvoice();

        invoice.setInvoiceID(json.getString("invoiceID"));
        invoice.setPaymentMethod(json.getString("paymentMethod"));
        invoice.setVatID(json.getString("vatID"));
        invoice.setTrnsactionID(json.getString("transactionID"));
        invoice.setCheckSum(json.getString("checkSum"));
        invoice.setDeviceID(json.getString("deviceID"));
        invoice.setDate(LocalDateTime.parse(json.getString("date")));

        JSONObject issuerJson = json.getJSONObject("issuer");
        invoice.setIssuer(Address.fromJSONObject(issuerJson));

        JSONArray itemsArray = json.getJSONArray("items");
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject itemJson = itemsArray.getJSONObject(i);
            Item item = Item.fromJSONObject(itemJson);
            invoice.addItem(item);
        }

        return invoice;
    }

    /**
     * Liefert eine String-Darstellung der Rechnung mit allen wichtigen Informationen.
     *
     * @return String-Repräsentation der Rechnung
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EateryInvoice {\n");
        sb.append("  Invoice ID     : ").append(invoiceID).append("\n");
        sb.append("  Net Price      : ").append(String.format("%.2f", netPrice)).append("€\n");
        sb.append("  Tax Price      : ").append(String.format("%.2f", taxPrice)).append("€\n");
        sb.append("  Gross Price    : ").append(String.format("%.2f", grossPrice)).append("€\n");
        sb.append("  Payment Method : ").append(paymentMethod).append("\n");
        sb.append("  Date           : ").append(date).append("\n");
        sb.append("  VAT ID         : ").append(vatID).append("\n");
        sb.append("  Transaction ID : ").append(trnsactionID).append("\n");
        sb.append("  Checksum       : ").append(checkSum).append("\n");
        sb.append("  Device ID      : ").append(deviceID).append("\n");
        sb.append("  Issuer         : ").append(issuer != null ? issuer.toString() : "null").append("\n");

        sb.append("  Items:\n");
        for (Item item : items) {
            sb.append("    - ").append(item.toString().replaceAll("(?m)^", "    ")).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }
}

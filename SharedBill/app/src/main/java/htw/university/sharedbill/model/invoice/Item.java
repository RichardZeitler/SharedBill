package htw.university.sharedbill.model.invoice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import htw.university.sharedbill.model.Utils;

/**
 * Repräsentiert einen einzelnen Posten (Item) einer Rechnung mit Namen, Beschreibung, Preisen und Steuersätzen.
 */
public class Item implements Serializable {
    private String name;
    private String description;
    private double netPrice;
    private double grossPrice;
    private double taxPrice;
    private double taxRate;

    /**
     * Erstellt einen neuen Artikel mit Namen, Brutto-Preis und Steuersatz.
     * Netto-Preis und Steuerbetrag werden automatisch berechnet.
     *
     * @param name       Der Name des Artikels.
     * @param grossPrice Der Brutto-Preis des Artikels.
     * @param taxRate    Der Steuersatz (z.B. 0.19 für 19% oder 19 für 19%).
     * @throws IllegalArgumentException wenn ungültige Werte übergeben werden.
     */
    public Item(String name, double grossPrice, double taxRate) {
        setName(name);
        setGrossPrice(grossPrice);
        setTaxRate(taxRate);
        setNetPrice(this.grossPrice / (1 + this.taxRate));
        setTaxPrice(this.grossPrice - this.netPrice);
        this.description = "keine";
    }

    /**
     * Erstellt einen neuen Artikel mit Namen, Beschreibung, Brutto-Preis und Steuersatz.
     *
     * @param name        Der Name des Artikels.
     * @param description Die Beschreibung des Artikels.
     * @param grossPrice  Der Brutto-Preis des Artikels.
     * @param taxRate     Der Steuersatz (z.B. 0.19 für 19% oder 19 für 19%).
     * @throws IllegalArgumentException wenn ungültige Werte übergeben werden.
     */
    public Item(String name, String description, double grossPrice, double taxRate) {
        this(name, grossPrice, taxRate);
        setDescription(description);
    }

    /**
     * Liefert den Namen des Artikels.
     *
     * @return Name des Artikels.
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert den Brutto-Preis des Artikels.
     *
     * @return Brutto-Preis.
     */
    public double getGrossPrice() {
        return grossPrice;
    }

    /**
     * Liefert den Netto-Preis des Artikels.
     *
     * @return Netto-Preis.
     */
    public double getNetPrice() {
        return netPrice;
    }

    /**
     * Liefert den Steuersatz des Artikels.
     *
     * @return Steuersatz als Dezimalzahl (z.B. 0.19 für 19%).
     */
    public double getTaxRate() {
        return taxRate;
    }

    /**
     * Liefert die Beschreibung des Artikels.
     *
     * @return Beschreibung.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Liefert den Steuerbetrag des Artikels.
     *
     * @return Steuerbetrag.
     */
    public double getTaxPrice() {
        return taxPrice;
    }

    /**
     * Setzt den Steuerbetrag.
     * Rundet auf zwei Dezimalstellen.
     *
     * @param taxPrice Steuerbetrag.
     * @throws IllegalArgumentException wenn der Steuerbetrag ungültig ist.
     */
    private void setTaxPrice(double taxPrice) {
        if (!Double.isFinite(taxPrice) || taxPrice < 0) throw new IllegalArgumentException("[Item] Steuerbetrag ist ungültig.");
        this.taxPrice = Utils.roundToDecimals(taxPrice, 2);
    }

    /**
     * Setzt den Namen des Artikels.
     *
     * @param name Name des Artikels.
     * @throws IllegalArgumentException wenn der Name null oder leer ist.
     */
    private void setName(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("[Item] Name ist ungültig.");
        this.name = name;
    }

    /**
     * Setzt den Steuersatz.
     * Akzeptiert Werte entweder als Dezimalzahl (z.B. 0.19) oder Prozent (z.B. 19).
     *
     * @param taxRate Steuersatz.
     * @throws IllegalArgumentException wenn der Steuersatz ungültig ist.
     */
    private void setTaxRate(double taxRate) {
        if (!Double.isFinite(taxRate)) throw new IllegalArgumentException("[Item] Mehrwertsteuer ist ungültig.");
        if (taxRate >= 1) this.taxRate = taxRate / 100;
        else this.taxRate = Utils.roundToDecimals(taxRate, 2);
    }

    /**
     * Setzt den Netto-Preis.
     * Rundet auf zwei Dezimalstellen.
     *
     * @param netPrice Netto-Preis.
     * @throws IllegalArgumentException wenn der Netto-Preis ungültig ist.
     */
    private void setNetPrice(double netPrice) {
        if (!Double.isFinite(netPrice)) throw new IllegalArgumentException("[Item] Nettobetrag ist ungültig.");
        else this.netPrice = Utils.roundToDecimals(netPrice, 2);
    }

    /**
     * Setzt den Brutto-Preis.
     * Rundet auf zwei Dezimalstellen.
     *
     * @param grossPrice Brutto-Preis.
     * @throws IllegalArgumentException wenn der Brutto-Preis ungültig ist.
     */
    private void setGrossPrice(double grossPrice) {
        if (!Double.isFinite(grossPrice) || grossPrice < 0) throw new IllegalArgumentException("[Item] Bruttobetrag ist ungültig.");
        this.grossPrice = Utils.roundToDecimals(grossPrice, 2);
    }

    /**
     * Setzt die Beschreibung. Falls leer oder null, wird "keine" gesetzt.
     *
     * @param description Beschreibung.
     */
    private void setDescription(String description) {
        if (description == null || description.isEmpty()) this.description = "keine";
        else this.description = description;
    }

    /**
     * Liefert eine lesbare String-Repräsentation des Artikels.
     *
     * @return String-Repräsentation.
     */
    @Override
    public String toString() {
        return  "Name: " + name +
                ", Description: " + description + System.lineSeparator() +
                "GrossPrice: " + String.format("%.2f€", grossPrice) +
                ", NetPrice: " + String.format("%.2f€", netPrice) + System.lineSeparator() +
                "TaxRate: " + String.format("%.2f", taxRate * 100) + "%" +
                ", TaxPrice: " + String.format("%.2f€", taxPrice);
    }

    /**
     * Wandelt das Item in ein JSON-Objekt um.
     *
     * @return JSON-Repräsentation des Artikels.
     * @throws JSONException bei Fehlern beim Erstellen des JSON-Objekts.
     */
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("name", name);
        json.put("description", description);
        json.put("netPrice", netPrice);
        json.put("grossPrice", grossPrice);
        json.put("taxPrice", taxPrice);
        json.put("taxRate", taxRate);

        return json;
    }

    /**
     * Erstellt ein Item-Objekt aus einem JSON-Objekt.
     *
     * @param json JSON-Objekt mit den Item-Daten.
     * @return Ein neues Item-Objekt.
     * @throws JSONException bei Fehlern beim Auslesen der JSON-Daten.
     */
    public static Item fromJSONObject(JSONObject json) throws JSONException {
        String name = json.getString("name");
        String description = json.optString("description", "keine");
        double netPrice = json.getDouble("netPrice");
        double grossPrice = json.getDouble("grossPrice");
        double taxPrice = json.getDouble("taxPrice");
        double taxRate = json.getDouble("taxRate");

        Item item = new Item(name, description, grossPrice, taxRate);
        item.setNetPrice(netPrice);
        item.setTaxPrice(taxPrice);
        return item;
    }

    /**
     * Vergleicht dieses Item mit einem anderen auf Gleichheit (Name, Brutto-Preis und Steuersatz).
     *
     * @param obj Das andere Objekt.
     * @return true, wenn gleich; sonst false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Item other = (Item) obj;
        return name.equals(other.name) &&
                Double.compare(grossPrice, other.grossPrice) == 0 &&
                Double.compare(taxRate, other.taxRate) == 0;
    }

    /**
     * Berechnet den Hash-Code basierend auf Name, Brutto-Preis und Steuersatz.
     *
     * @return Hash-Code.
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        long temp;

        temp = Double.doubleToLongBits(grossPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(taxRate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));

        return result;
    }

}

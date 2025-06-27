package htw.university.sharedbill.model.invoice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private String description;
    private double netPrice;
    private double grossPrice;
    private double taxPrice;
    private double taxRate;

    public Item(String name, double grossPrice, double taxRate) {
        setName(name);
        setGrossPrice(grossPrice);
        setTaxRate(taxRate);
        setNetPrice(this.grossPrice / (1 + this.taxRate));
        setTaxPrice(this.grossPrice - this.netPrice);
        this.description = "keine";
    }

    public Item(String name, String description, double grossPrice, double taxRate) {
        this(name, grossPrice, taxRate);
        setDescription(description);
    }

    public String getName() {
        return name;
    }

    public double getGrossPrice() {
        return grossPrice;
    }

    public double getNetPrice() {
        return netPrice;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public String getDescription() {
        return description;
    }

    public double getTaxPrice() {
        return taxPrice;
    }

    private void setTaxPrice(double taxPrice) {
        if (!Double.isFinite(taxPrice) || taxPrice < 0) throw new IllegalArgumentException("[Item] Steuerbetrag ist ungültig.");
        this.taxPrice = taxPrice;
    }

    private void setName(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("[Item] Name ist ungültig.");
        this.name = name;
    }

    private void setTaxRate(double taxRate) {
        if (!Double.isFinite(taxRate)) throw new IllegalArgumentException("[Item] Mehrwertsteuer ist ungültig.");
        if (taxRate >= 1) this.taxRate = taxRate / 100;
        else this.taxRate = taxRate;
    }

    private void setNetPrice(double netPrice) {
        if (!Double.isFinite(netPrice)) throw new IllegalArgumentException("[Item] Nettobetrag ist ungültig.");
        else this.netPrice = netPrice;
    }

    private void setGrossPrice(double grossPrice) {
        if (!Double.isFinite(grossPrice) || grossPrice < 0) throw new IllegalArgumentException("[Item] Bruttobetrag ist ungültig.");
        this.grossPrice = grossPrice;
    }

    private void setDescription(String description) {
        if (description == null || description.isEmpty()) this.description = "keine";
        else this.description = description;
    }

    @Override
    public String toString() {
        return  "Name: " + name +
                ", Description: " + description + System.lineSeparator() +
                "GrossPrice: " + String.format("%.2f€", grossPrice) +
                ", NetPrice: " + String.format("%.2f€", netPrice) + System.lineSeparator() +
                "TaxRate: " + String.format("%.2f", taxRate * 100) + "%" +
                ", TaxPrice: " + String.format("%.2f€", taxPrice);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Item other = (Item) obj;

        return Double.compare(other.netPrice, netPrice) == 0 &&
                Double.compare(other.grossPrice, grossPrice) == 0 &&
                Double.compare(other.taxPrice, taxPrice) == 0 &&
                Double.compare(other.taxRate, taxRate) == 0 &&
                name.equals(other.name) &&
                description.equals(other.description);
    }

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

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        result = 31 * result + description.hashCode();
        temp = Double.doubleToLongBits(netPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(grossPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(taxPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(taxRate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

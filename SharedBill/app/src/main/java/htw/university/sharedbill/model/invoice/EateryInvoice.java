package htw.university.sharedbill.model.invoice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<Item, Integer> items = new HashMap<>();
    private transient List<InvoiceObserver> invoiceObserverList = new ArrayList<>();

    public void addObserver(InvoiceObserver invoiceObserver) {
        if (invoiceObserver != null && !invoiceObserverList.contains(invoiceObserver))
            invoiceObserverList.add(invoiceObserver);
    }

    public void removeObserver(InvoiceObserver invoiceObserver) {
        invoiceObserverList.remove(invoiceObserver);
    }

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
    public Map<Item, Integer> getItems() {
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

    @Override
    public void addItem(Item item) {
        if (item == null) throw new IllegalArgumentException("[EateryInvoice] Item zum Hinzufügen ist ungültig.");
        items.put(item, items.getOrDefault(item, 0) + 1);

        setNetPrice(netPrice + item.getNetPrice());
        setGrossPrice(grossPrice + item.getGrossPrice());
        setTaxPrice(taxPrice + item.getTaxPrice());

        notifyObserver();
    }

    @Override
    public void removeItem(Item item) {
        if (item == null) throw new IllegalArgumentException("[EateryInvoice] Item zum Entfernen ist ungültig.");
        if (items.containsKey(item)) {
            items.put(item, items.get(item) - 1);
            if (items.get(item) <= 0)
                items.remove(item);

            setNetPrice(netPrice - item.getNetPrice());
            setGrossPrice(grossPrice - item.getGrossPrice());
            setTaxPrice(taxPrice - item.getTaxPrice());

            notifyObserver();
        }
    }

    public String getDeviceID() {
        return deviceID;
    }

    private void setNetPrice(double netPrice) {
        if (!Double.isFinite(netPrice) || netPrice < 0) throw new IllegalArgumentException("[EateryInvoice] Nettopreis ist ungültig.");
        this.netPrice = netPrice;
    }


    private void setTaxPrice(double taxPrice) {
        if (!Double.isFinite(taxPrice) || taxPrice < 0) throw new IllegalArgumentException("[EateryInvoice] Steuerbetrag ist ungültig.");
        this.taxPrice = taxPrice;
    }

    private void setGrossPrice(double grossPrice) {
        if (!Double.isFinite(grossPrice) || grossPrice < 0) throw new IllegalArgumentException("[EateryInvoice] Bruttobetrag ist ungültig.");
        this.grossPrice = grossPrice;
    }

    public void setPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Zahlungsmethode ist ungültig.");
        this.paymentMethod = paymentMethod;
    }

    public void setIssuer(Address issuer) {
        if (issuer == null) throw new IllegalArgumentException("[EateryInvoice] Rechnungsaussteller ist ungültig.");
        this.issuer = issuer;
    }

    public void setCheckSum(String checkSum) {
        if (checkSum == null || checkSum.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Checksumme ist ungültig.");
        this.checkSum = checkSum;
    }

    public void setDate(LocalDateTime date) {
        if (date == null) throw new IllegalArgumentException("[EateryInvoice] Ausstellungsdatum ist ungültig.");
        this.date = date;
    }

    public void setDeviceID(String deviceID) {
        if (deviceID == null || deviceID.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Geräteid ist ungültig.");
        this.deviceID = deviceID;
    }

    public void setInvoiceID(String invoiceID) {
        if (invoiceID == null || invoiceID.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Rechnungsid ist ungültig.");
        this.invoiceID = invoiceID;
    }

    public void setTrnsactionID(String trnsactionID) {
        if (trnsactionID == null || trnsactionID.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Transaktionsid ist ungültig.");
        this.trnsactionID = trnsactionID;
    }

    public void setVatID(String vatID) {
        if (vatID == null || vatID.isEmpty()) throw new IllegalArgumentException("[EateryInvoice] Steueridentifikationsnummer ist ungültig.");
        this.vatID = vatID;
    }

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

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(date.toString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        String creationDateString = (String) in.readObject();
        this.date = LocalDateTime.parse(creationDateString);
    }

}

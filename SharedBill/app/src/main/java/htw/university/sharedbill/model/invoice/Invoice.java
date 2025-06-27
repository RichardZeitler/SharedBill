package htw.university.sharedbill.model.invoice;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Invoice {

    Address getIssuer();
    String getInvoiceID();
    Map<Item, Integer> getItems();
    double getNetPrice();
    double getGrossPrice();
    double getTaxPrice();
    String getPaymentMethod();
    LocalDateTime getDate();
    String getVatID();
    String getTransactionID();
    String getCheckSum();
    void addItem(Item item);
    void removeItem(Item item);
    JSONObject getJSONObject() throws JSONException;

    static Invoice fromJSONObject(JSONObject json) throws JSONException {
        return EateryInvoice.fromJSONObject(json);
    }
}

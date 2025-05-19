package htw.university.sharedbill.model.invoice;

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
}

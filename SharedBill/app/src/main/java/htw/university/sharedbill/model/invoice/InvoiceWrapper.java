package htw.university.sharedbill.model.invoice;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InvoiceWrapper implements Serializable {
    private String paymentStatus;
    private Invoice invoice;

    public static List<InvoiceWrapper> INVOICES = new ArrayList<>();


    public InvoiceWrapper(Invoice invoice, String paymentStatus) {
        setInvoice(invoice);
        setPaymentStatus(paymentStatus);
    }


    private void setInvoice(Invoice invoice) {
        if (invoice == null) throw new IllegalArgumentException("[InvoiceWrapper] Rechnung ist ungültig.");
        this.invoice = invoice;
    }

    public void setPaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isEmpty()) throw new IllegalArgumentException("[InvoiceWrapper] Rechnungstatus ist ungültig.");
        this.paymentStatus = paymentStatus;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        InvoiceWrapper that = (InvoiceWrapper) obj;
        return invoice != null && invoice.getInvoiceID().equals(that.invoice.getInvoiceID());
    }

    @Override
    public int hashCode() {
        return invoice != null ? invoice.getInvoiceID().hashCode() : 0;
    }

}

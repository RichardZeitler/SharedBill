package htw.university.sharedbill.model.invoice;

/**
 * Interface für Beobachter von Rechnungen (Observer im Observer-Pattern).
 * Implementierende Klassen werden benachrichtigt, wenn sich eine Rechnung ändert.
 */
public interface InvoiceObserver {

    /**
     * Wird aufgerufen, wenn eine Rechnung aktualisiert wurde.
     *
     * @param invoice Die aktualisierte Rechnung.
     */
    void updateInvoice(Invoice invoice);
}

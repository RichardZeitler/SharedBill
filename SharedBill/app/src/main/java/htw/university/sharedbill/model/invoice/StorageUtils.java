package htw.university.sharedbill.model.invoice;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {

    /**
     * Speichert eine Rechnung als JSON-Datei im App-externen Speicher.
     *
     * @param context        Kontext der App
     * @param invoiceWrapper Rechnung zum Speichern
     * @return true wenn erfolgreich, sonst false
     * @throws IOException   falls ein Schreibfehler auftritt
     * @throws JSONException falls ein Fehler beim JSON-Handling auftritt
     * @throws IllegalStateException falls Speicherverzeichnis nicht vorhanden
     */
    public static boolean saveInvoiceToAppStorage(Context context, InvoiceWrapper invoiceWrapper) throws IOException, JSONException {
        File folder = context.getExternalFilesDir("Invoices");
        if (folder == null) {
            throw new IllegalStateException("Speicherverzeichnis konnte nicht erstellt werden.");
        }

        Invoice invoice = invoiceWrapper.getInvoice();
        String fileName = invoice.getInvoiceID() + ".json";

        File file = new File(folder, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            JSONObject json = invoiceWrapper.getJSONObject();
            writer.write(json.toString(4));
        }
        return true;
    }

    /**
     * Lädt alle Rechnungen aus dem App-externen Speicher.
     *
     * @param context Kontext der App
     * @return Liste aller geladenen InvoiceWrapper
     * @throws IOException   falls ein Lesefehler auftritt
     * @throws JSONException falls ein Fehler beim JSON-Parsing auftritt
     * @throws IllegalStateException falls Verzeichnis nicht existiert
     */
    public static List<InvoiceWrapper> loadAllInvoices(Context context) throws IOException, JSONException {
        List<InvoiceWrapper> invoiceList = new ArrayList<>();
        File folder = context.getExternalFilesDir("Invoices");

        if (folder == null || !folder.exists()) {
            throw new IllegalStateException("Rechnungsordner nicht vorhanden.");
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return invoiceList;

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder jsonStringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonStringBuilder.append(line);
                }
                JSONObject json = new JSONObject(jsonStringBuilder.toString());
                InvoiceWrapper invoiceWrapper = InvoiceWrapper.fromJSONObject(json);
                invoiceList.add(invoiceWrapper);
            }
        }
        return invoiceList;
    }

    /**
     * Löscht eine gespeicherte Rechnung.
     *
     * @param context Kontext der App
     * @param invoice Rechnung, die gelöscht werden soll
     * @return true wenn erfolgreich gelöscht, sonst false
     * @throws IllegalStateException falls Verzeichnis nicht vorhanden
     */
    public static boolean deleteInvoiceFromStorage(Context context, Invoice invoice) {
        File folder = context.getExternalFilesDir("Invoices");
        if (folder == null || !folder.exists()) {
            throw new IllegalStateException("Rechnungsordner nicht vorhanden.");
        }

        String fileName = invoice.getInvoiceID() + ".json";

        File file = new File(folder, fileName);
        if (file.exists()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /**
     * Aktualisiert eine bestehende Rechnung, indem sie gelöscht und neu gespeichert wird.
     *
     * @param context               Kontext der App
     * @param updatedInvoiceWrapper Aktualisierte Rechnung
     * @return true bei Erfolg, sonst false
     * @throws IOException          falls Fehler beim Schreiben auftritt
     * @throws JSONException        falls Fehler beim JSON-Handling auftritt
     * @throws IllegalStateException falls Verzeichnis nicht vorhanden
     */
    public static boolean updateInvoiceInStorage(Context context, InvoiceWrapper updatedInvoiceWrapper) throws IOException, JSONException {
        File folder = context.getExternalFilesDir("Invoices");
        if (folder == null || !folder.exists()) {
            throw new IllegalStateException("Rechnungsordner nicht vorhanden.");
        }

        Invoice updatedInvoice = updatedInvoiceWrapper.getInvoice();
        String fileName = updatedInvoice.getInvoiceID() + ".json";
        File file = new File(folder, fileName);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new IOException("Fehler beim Löschen der alten Rechnung.");
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            JSONObject json = updatedInvoiceWrapper.getJSONObject();
            writer.write(json.toString(4));
        }
        return true;
    }
}

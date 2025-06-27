package htw.university.sharedbill.model.invoice;

import android.content.Context;
import android.widget.Toast;

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

public class InvoiceUtils {

    public static void saveInvoiceToAppStorage(Context context, InvoiceWrapper invoiceWrapper) {
        File folder = context.getExternalFilesDir("Invoices");
        if (folder == null) {
            Toast.makeText(context, "Fehler: Speicherverzeichnis konnte nicht erstellt werden.", Toast.LENGTH_SHORT).show();
            return;
        }

        Invoice invoice = invoiceWrapper.getInvoice();
        String fileName = invoice.getInvoiceID() + ".json";
        File file = new File(folder, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            JSONObject json = invoiceWrapper.getJSONObject();
            writer.write(json.toString(4)); // mit Einrückung für bessere Lesbarkeit
        } catch (IOException | JSONException e) {
            Toast.makeText(context, "Fehler beim Speichern der Rechnung.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static List<InvoiceWrapper> loadAllInvoices(Context context) {
        List<InvoiceWrapper> invoiceList = new ArrayList<>();
        File folder = context.getExternalFilesDir("Invoices");

        if (folder == null || !folder.exists()) {
            Toast.makeText(context, "Fehler: Rechnungsordner nicht vorhanden.", Toast.LENGTH_SHORT).show();
            return invoiceList;
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

            } catch (IOException | JSONException e) {
                Toast.makeText(context, "Fehler beim Laden der Datei: " + file.getName(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        Toast.makeText(context, "Anzahl geladener Rechnungen: " + invoiceList.size(), Toast.LENGTH_SHORT).show();
        return invoiceList;
    }

    public static boolean deleteInvoiceFromStorage(Context context, Invoice invoice) {
        File folder = context.getExternalFilesDir("Invoices");
        if (folder == null || !folder.exists()) {
            Toast.makeText(context, "Fehler: Rechnungsordner nicht vorhanden.", Toast.LENGTH_SHORT).show();
            return false;
        }

        String fileName = invoice.getInvoiceID() + ".json";

        File file = new File(folder, fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(context, "Rechnung gelöscht.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Fehler beim Löschen der Rechnung.", Toast.LENGTH_SHORT).show();
            }
            return deleted;
        } else {
            Toast.makeText(context, "Datei nicht gefunden: " + fileName, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean updateInvoiceInStorage(Context context, InvoiceWrapper updatedInvoiceWrapper) {
        File folder = context.getExternalFilesDir("Invoices");
        if (folder == null || !folder.exists()) {
            Toast.makeText(context, "Fehler: Rechnungsordner nicht vorhanden.", Toast.LENGTH_SHORT).show();
            return false;
        }

        Invoice updatedInvoice = updatedInvoiceWrapper.getInvoice();
        String fileName = updatedInvoice.getInvoiceID() + ".json";
        File file = new File(folder, fileName);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                Toast.makeText(context, "Fehler beim Löschen der alten Rechnung.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            JSONObject json = updatedInvoiceWrapper.getJSONObject();
            writer.write(json.toString(4));
            Toast.makeText(context, "Rechnung erfolgreich aktualisiert.", Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException | JSONException e) {
            Toast.makeText(context, "Fehler beim Speichern der aktualisierten Rechnung.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
    }
}

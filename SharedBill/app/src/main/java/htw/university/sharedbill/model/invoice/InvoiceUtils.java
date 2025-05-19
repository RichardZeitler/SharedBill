package htw.university.sharedbill.model.invoice;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
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


        String fileName = invoice.getInvoiceID() + ".ser";
        File file = new File(folder, fileName);

        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            out.writeObject(invoiceWrapper);

        } catch (IOException e) {
            Toast.makeText(context, "Fehler beim Speichern der Rechnung.", Toast.LENGTH_SHORT).show();
        }
    }

    public static List<InvoiceWrapper> loadAllInvoices(Context context) {
        List<InvoiceWrapper> invoiceList = new ArrayList<>();
        File folder = context.getExternalFilesDir("Invoices");

        if (folder == null || !folder.exists()) {
            Toast.makeText(context, "Fehler: Rechnungsordner nicht vorhanden.", Toast.LENGTH_SHORT).show();
            return invoiceList;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".ser"));
        if (files == null) return invoiceList;

        for (File file : files) {
            try (FileInputStream fileIn = new FileInputStream(file);
                 ObjectInputStream in = new ObjectInputStream(fileIn)) {

                InvoiceWrapper invoice = (InvoiceWrapper) in.readObject();
                invoiceList.add(invoice);

            } catch (IOException | ClassNotFoundException e) {
                Toast.makeText(context, "Fehler beim Laden der Datei: " + file.getName(), Toast.LENGTH_SHORT).show();
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

        String fileName = invoice.getInvoiceID() + ".ser";

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
        String fileName = updatedInvoice.getInvoiceID() + ".ser";
        File file = new File(folder, fileName);

        // Alte Datei löschen (wenn vorhanden)
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                Toast.makeText(context, "Fehler beim Löschen der alten Rechnung.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Neue Version speichern
        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            out.writeObject(updatedInvoiceWrapper);
            Toast.makeText(context, "Rechnung erfolgreich aktualisiert.", Toast.LENGTH_SHORT).show();
            return true;

        } catch (IOException e) {
            Toast.makeText(context, "Fehler beim Speichern der aktualisierten Rechnung.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


}

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

    public static void saveInvoiceToAppStorage(Context context, Invoice invoice) {
        File folder = context.getExternalFilesDir("Invoices");
        if (folder == null) {
            Toast.makeText(context, "Fehler: Speicherverzeichnis konnte nicht erstellt werden.", Toast.LENGTH_SHORT).show();
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDate = invoice.getDate().format(formatter);

        String fileName = invoice.getInvoiceID() + "_" + formattedDate + ".ser";
        File file = new File(folder, fileName);

        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

            out.writeObject(invoice);

        } catch (IOException e) {
            Toast.makeText(context, "Fehler beim Speichern der Rechnung.", Toast.LENGTH_SHORT).show();
        }
    }

    public static List<Invoice> loadAllInvoices(Context context) {
        List<Invoice> invoiceList = new ArrayList<>();
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

                Invoice invoice = (Invoice) in.readObject();
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDate = invoice.getDate().format(formatter);
        String fileName = invoice.getInvoiceID() + "_" + formattedDate + ".ser";

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

}

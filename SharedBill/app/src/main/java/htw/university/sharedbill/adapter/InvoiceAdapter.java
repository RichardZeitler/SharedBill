package htw.university.sharedbill.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import htw.university.sharedbill.R;
import htw.university.sharedbill.controller.bluetooth.SelectDeviceActivity;
import htw.university.sharedbill.controller.invoce.InvoiceShowActivity;
import htw.university.sharedbill.model.invoice.Invoice;
import htw.university.sharedbill.model.invoice.StorageUtils;
import htw.university.sharedbill.model.invoice.InvoiceWrapper;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {
    private final Context context;

    public InvoiceAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.invoice_item_layout, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        InvoiceWrapper invoiceWrapper = InvoiceWrapper.INVOICES.get(position);
        Invoice invoice = invoiceWrapper.getInvoice();

        holder.invoiceText.setText("Rechnung: # " + invoice.getInvoiceID());

        // Set the default payment status in TextView
        holder.paymentStatusTextView.setText(invoiceWrapper.getPaymentStatus()); // Default status

        // Show the options for payment status selection when clicking on the TextView
        holder.paymentStatusTextView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenu().add("Unbezahlt");
            popupMenu.getMenu().add("Bezahlt");
            popupMenu.getMenu().add("Wartend");

            popupMenu.setOnMenuItemClickListener(item -> {
                String selectedStatus = item.getTitle().toString();
                invoiceWrapper.setPaymentStatus(selectedStatus);
                try {
                    StorageUtils.updateInvoiceInStorage(context, invoiceWrapper);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                holder.paymentStatusTextView.setText(selectedStatus); // Update the text of the TextView
                Toast.makeText(context, "Status ausgewählt: " + selectedStatus, Toast.LENGTH_SHORT).show();
                return true;
            });

            popupMenu.show();
        });

        // Handle Popup menu for menu icon
        holder.menuIcon.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.menuIcon);
            popupMenu.getMenu().add("Anzeigen");
            popupMenu.getMenu().add("Teilen");
            popupMenu.getMenu().add("Löschen");

            popupMenu.setOnMenuItemClickListener(item -> handleMenuAction(item, invoiceWrapper, position));
            popupMenu.show();
        });
    }

    private boolean handleMenuAction(MenuItem item, InvoiceWrapper invoiceWrapper, int position) {
        Invoice invoice = invoiceWrapper.getInvoice();
        String title = item.getTitle().toString();
        Intent intent = null;
        switch (title) {
            case "Anzeigen":
                intent = new Intent(context, InvoiceShowActivity.class);
                intent.putExtra("invoice", (Serializable) invoiceWrapper);
                context.startActivity(intent);
                return true;
            case "Teilen":
                intent = new Intent(context, SelectDeviceActivity.class);
                intent.putExtra("title", "Verbundene Bluetooth-Geräte");
                intent.putExtra("disableScan", true);
                intent.putExtra("invoice", (Serializable) invoiceWrapper);
                context.startActivity(intent);
                return true;
            case "Löschen":
                boolean deleted = StorageUtils.deleteInvoiceFromStorage(context, invoice);
                if (deleted) {
                    int index = InvoiceWrapper.INVOICES.indexOf(invoiceWrapper); // <- Korrekt den Index holen
                    if (index != -1) {
                        InvoiceWrapper.INVOICES.remove(index);
                        notifyItemRemoved(index);
                        Toast.makeText(context, "Rechnung gelöscht.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Fehler: Rechnung nicht gefunden.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Fehler beim Löschen.", Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    public int getItemCount() {
        return InvoiceWrapper.INVOICES.size();
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView invoiceText;
        TextView paymentStatusTextView;  // TextView für den Zahlungsstatus
        ImageView menuIcon;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            invoiceText = itemView.findViewById(R.id.invoiceText);
            menuIcon = itemView.findViewById(R.id.invoiceMenuIcon);
            paymentStatusTextView = itemView.findViewById(R.id.paymentStatusTextView); // TextView initialisieren
        }
    }

    // Dummy-Methode für Zahlungsstatusoptionen, ersetze sie mit tatsächlichen Daten
    private List<String> getPaymentStatusOptions() {
        // Dies ist ein Beispiel, du kannst es durch dynamische Daten ersetzen
        return Arrays.asList("Unbezahlt", "Bezahlt", "Wartend");
    }
}

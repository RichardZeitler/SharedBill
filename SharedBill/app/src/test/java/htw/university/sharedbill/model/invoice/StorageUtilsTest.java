package htw.university.sharedbill.model.invoice;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Unit-Tests für die Klasse {@link StorageUtils}.
 *
 * Diese Testklasse überprüft das Speichern und Laden von Rechnungen im App-Speicher.
 * Es wird Robolectric verwendet, um den Android-Kontext zu simulieren.
 */
@RunWith(RobolectricTestRunner.class)
public class StorageUtilsTest {

    private File tempDir;
    private InvoiceWrapper invoiceWrapper;
    private EateryInvoice invoice;
    private Address issuer;
    private Item item;

    @Mock
    Context mockContext;

    /**
     * Bereitet die Testumgebung vor.
     * Erstellt ein temporäres Verzeichnis, das den App-Speicher simuliert,
     * und initialisiert die Rechnungsobjekte mit Testdaten.
     *
     * @throws Exception falls die Vorbereitung fehlschlägt
     */
    @Before
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("invoices_test").toFile();

        mockContext = mock(Context.class);
        when(mockContext.getExternalFilesDir("Invoices")).thenReturn(tempDir);

        invoice = new EateryInvoice();

        issuer = new Address("Name", "Street", 123456, "City", "Country");
        item = new Item("Item", 2, 0.07);

        invoice.setIssuer(issuer);
        invoice.setInvoiceID("INV123");
        invoice.setPaymentMethod("Bar");
        invoice.setDate(LocalDateTime.now());
        invoice.setVatID("VAT123");
        invoice.setTrnsactionID("TRANSACTION123");
        invoice.setCheckSum("CHECK123");
        invoice.setDeviceID("DEVICE123");

        invoice.addItem(item);

        invoiceWrapper = new InvoiceWrapper(invoice, "Bar");
    }

    /**
     * Säubert die Testumgebung nach jedem Test.
     * Löscht temporäre Dateien und Verzeichnisse.
     *
     * @throws Exception falls die Aufräumarbeiten fehlschlagen
     */
    @After
    public void tearDown() throws Exception {
        for (File f : tempDir.listFiles()) {
            f.delete();
        }
        tempDir.delete();
    }

    /**
     * Testet, ob eine Rechnung korrekt im App-Speicher gespeichert wird.
     *
     * @throws IOException falls das Speichern fehlschlägt
     */
    @Test
    public void testSaveFile() throws IOException, JSONException {
        StorageUtils.saveInvoiceToAppStorage(mockContext, invoiceWrapper);

        File savedFile = new File(tempDir, "INV123.json");
        assertTrue("Datei sollte gespeichert sein", savedFile.exists());
    }

    /**
     * Testet, ob gespeicherte Rechnungen korrekt aus dem App-Speicher geladen werden können.
     * Überprüft, ob die geladene Rechnung mit der gespeicherten übereinstimmt.
     *
     * @throws IOException falls das Laden oder Speichern fehlschlägt
     */
    @Test
    public void testLoadFile() throws IOException, JSONException {
        StorageUtils.saveInvoiceToAppStorage(mockContext, invoiceWrapper);

        List<InvoiceWrapper> loadedInvoices = StorageUtils.loadAllInvoices(mockContext);

        assertFalse("Liste der geladenen Rechnungen sollte nicht leer sein", loadedInvoices.isEmpty());

        InvoiceWrapper loadedInvoiceWrapper = loadedInvoices.get(0);
        assertEquals("InvoiceID sollte übereinstimmen", invoiceWrapper.getInvoice().getInvoiceID(), loadedInvoiceWrapper.getInvoice().getInvoiceID());
    }

}

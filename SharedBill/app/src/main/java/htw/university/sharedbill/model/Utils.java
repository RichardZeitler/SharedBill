package htw.university.sharedbill.model;

import android.content.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Hilfsklasse mit nützlichen Methoden für mathematische Operationen.
 */
public class Utils {

    /**
     * Rundet einen double-Wert auf die angegebene Anzahl Nachkommastellen.
     *
     * @param value         Der zu rundende Wert.
     * @param decimalPlaces Die Anzahl der Nachkommastellen (muss 0 oder größer sein).
     * @return Der gerundete Wert als double.
     * @throws IllegalArgumentException Wenn decimalPlaces negativ ist.
     */
    public static double roundToDecimals(double value, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("Die Anzahl der Nachkommastellen darf nicht negativ sein.");
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Wandelt einen Wert in dp (Density-independent Pixels) in Pixel um,
     * basierend auf der aktuellen Bildschirmdichte des Geräts.
     *
     * @param context Der Kontext, um auf die Ressourcen und Display-Metriken zuzugreifen.
     *                Darf nicht null sein.
     * @param dp      Der Wert in dp, der umgerechnet werden soll. Muss 0 oder größer sein.
     * @return Der umgerechnete Wert in Pixel als Ganzzahl.
     * @throws IllegalArgumentException Wenn der Kontext null ist oder dp negativ ist.
     */
    public static int dpToPx(Context context, int dp) {
        if (context == null) throw new IllegalArgumentException("[Utils] Context ist ungültig.");
        if (dp < 0) throw new IllegalArgumentException("[Utils] dp ist ungültig.");

        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

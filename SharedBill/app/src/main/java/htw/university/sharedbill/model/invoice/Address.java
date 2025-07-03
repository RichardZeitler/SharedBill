package htw.university.sharedbill.model.invoice;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Repräsentiert eine Adresse mit Namen, Straße, Postleitzahl, Stadt und Land.
 */
public class Address implements Serializable {
    private String name;
    private String street;
    private int zip;
    private String city;
    private String country;

    /**
     * Erstellt eine neue Adresse mit allen erforderlichen Feldern.
     *
     * @param name    Name der Person oder Firma.
     * @param street  Straße inklusive Hausnummer.
     * @param zip     Postleitzahl (positiv).
     * @param city    Stadt.
     * @param country Land.
     * @throws IllegalArgumentException wenn eines der Felder ungültig ist.
     */
    public Address(String name, String street, int zip, String city, String country) {
        setName(name);
        setStreet(street);
        setCity(city);
        setZip(zip);
        setCountry(country);
    }

    /**
     * Liefert den Namen der Adresse (Person oder Firma).
     *
     * @return Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert die Postleitzahl.
     *
     * @return Postleitzahl.
     */
    public int getZip() {
        return zip;
    }

    /**
     * Liefert die Stadt.
     *
     * @return Stadt.
     */
    public String getCity() {
        return city;
    }

    /**
     * Liefert das Land.
     *
     * @return Land.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Liefert die Straße mit Hausnummer.
     *
     * @return Straße.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Setzt den Namen der Adresse.
     *
     * @param name Name (nicht null oder leer).
     * @throws IllegalArgumentException wenn Name ungültig ist.
     */
    private void setName(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("[Address] Name ist ungültig.");
        this.name = name;
    }

    /**
     * Setzt die Stadt.
     *
     * @param city Stadt (nicht null oder leer).
     * @throws IllegalArgumentException wenn Stadt ungültig ist.
     */
    private void setCity(String city) {
        if (city == null || city.isEmpty()) throw new IllegalArgumentException("[Address] Stadt ist ungültig.");
        this.city = city;
    }

    /**
     * Setzt das Land.
     *
     * @param country Land (nicht null oder leer).
     * @throws IllegalArgumentException wenn Land ungültig ist.
     */
    private void setCountry(String country) {
        if (country == null || country.isEmpty()) throw new IllegalArgumentException("[Address] Land ist ungültig.");
        this.country = country;
    }

    /**
     * Setzt die Straße.
     *
     * @param street Straße (nicht null oder leer).
     * @throws IllegalArgumentException wenn Straße ungültig ist.
     */
    private void setStreet(String street) {
        if (street == null || street.isEmpty()) throw new IllegalArgumentException("[Address] Straße ist ungültig.");
        this.street = street;
    }

    /**
     * Setzt die Postleitzahl.
     *
     * @param zip Postleitzahl (muss >= 0 sein).
     * @throws IllegalArgumentException wenn Postleitzahl ungültig ist.
     */
    private void setZip(int zip) {
        if (zip < 0) throw new IllegalArgumentException("[Address] Postleitzahl ist ungültig.");
        this.zip = zip;
    }

    /**
     * Vergleicht diese Adresse mit einer anderen auf Gleichheit.
     * Berücksichtigt alle Felder.
     *
     * @param obj Das andere Objekt.
     * @return true, wenn alle Felder gleich sind, sonst false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Address address = (Address) obj;
        return zip == address.zip &&
                name.equals(address.name) &&
                street.equals(address.street) &&
                city.equals(address.city) &&
                country.equals(address.country);
    }

    /**
     * Gibt eine String-Repräsentation der Adresse zurück.
     *
     * @return String-Repräsentation.
     */
    @Override
    public String toString() {
        return "Address{" +
                "name='" + name + '\'' +
                ", street='" + street + '\'' +
                ", zip=" + zip +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }

    /**
     * Wandelt die Adresse in ein JSON-Objekt um.
     *
     * @return JSON-Objekt mit allen Adressfeldern.
     * @throws JSONException falls ein Fehler beim Erstellen auftritt.
     */
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("name", name);
        json.put("street", street);
        json.put("zip", zip);
        json.put("city", city);
        json.put("country", country);

        return json;
    }

    /**
     * Erzeugt eine Adresse aus einem JSON-Objekt.
     *
     * @param json JSON-Objekt mit den Adressdaten.
     * @return Neue Address-Instanz.
     * @throws JSONException falls ein Fehler beim Auslesen des JSON auftritt.
     */
    public static Address fromJSONObject(JSONObject json) throws JSONException {
        String name = json.getString("name");
        String street = json.getString("street");
        int zip = json.getInt("zip");
        String city = json.getString("city");
        String country = json.getString("country");

        return new Address(name, street, zip, city, country);
    }
}

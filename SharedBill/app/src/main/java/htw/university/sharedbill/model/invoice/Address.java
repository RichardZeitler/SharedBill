package htw.university.sharedbill.model.invoice;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Address implements Serializable {
    private String name;
    private String street;
    private int zip;
    private String city;
    private String country;

    public Address(String name, String street, int zip, String city, String country) {
        setName(name);
        setStreet(street);
        setCity(city);
        setZip(zip);
        setCountry(country);
    }

    public String getName() {
        return name;
    }

    public int getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getStreet() {
        return street;
    }

    private void setName(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("[Address] Name ist ungültig.");
        this.name = name;
    }

    private void setCity(String city) {
        if (city == null || city.isEmpty()) throw new IllegalArgumentException("[Address] Stadt ist ungültig.");
        this.city = city;
    }

    private void setCountry(String country) {
        if (country == null || country.isEmpty()) throw new IllegalArgumentException("[Address] Land ist ungültig.");
        this.country = country;
    }

    private void setStreet(String street) {
        if (street == null || street.isEmpty()) throw new IllegalArgumentException("[Address] Straße ist ungültig.");
        this.street = street;
    }

    private void setZip(int zip) {
        if (zip < 0) throw new IllegalArgumentException("[Address] Postleitzahl ist ungültig.");
        this.zip = zip;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Address address = (Address) obj; // Casten des Objekts in Address
        return zip == address.zip &&
                name.equals(address.name) &&
                street.equals(address.street) &&
                city.equals(address.city) &&
                country.equals(address.country);
    }

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

    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("name", name);
        json.put("street", street);
        json.put("zip", zip);
        json.put("city", city);
        json.put("country", country);

        return json;
    }

    public static Address fromJSONObject(JSONObject json) throws JSONException {
        String name = json.getString("name");
        String street = json.getString("street");
        int zip = json.getInt("zip");
        String city = json.getString("city");
        String country = json.getString("country");
        return new Address(name, street, zip, city, country);
    }
}

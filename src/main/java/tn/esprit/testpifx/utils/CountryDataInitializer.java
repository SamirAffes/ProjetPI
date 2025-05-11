package tn.esprit.testpifx.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for initializing and providing country data in the application.
 * This class centralizes country data management similar to UserDataInitializer.
 */
public class CountryDataInitializer {

    private static final List<String> COUNTRIES = Arrays.asList(
        "Select a country", // Default option
        "Tunisia"
        // Commented out other countries to only show Tunisia
        /*
        "Algeria",
        "Morocco",
        "Egypt",
        "South Africa",
        "Nigeria",
        "Kenya",
        "United States",
        "Canada",
        "United Kingdom",
        "France",
        "Germany",
        "Italy",
        "Spain",
        "Australia",
        "Japan",
        "China",
        "India",
        "Brazil",
        "Mexico",
        "South Africa",
        "Egypt",
        "Tunisia",
        "Morocco",
        "Algeria",
        "Nigeria",
        "Kenya"
        */
    );
    
    /**
     * List of Tunisian governorates.
     */
    private static final List<String> TUNISIAN_GOVERNORATES = Arrays.asList(
        "Select a governorate", // Default option
        "Ariana",
        "Béja",
        "Ben Arous",
        "Bizerte",
        "Gabès",
        "Gafsa",
        "Jendouba",
        "Kairouan",
        "Kasserine",
        "Kébili",
        "Kef",
        "Mahdia",
        "Manouba",
        "Médenine",
        "Monastir",
        "Nabeul",
        "Sfax",
        "Sidi Bouzid",
        "Siliana",
        "Sousse",
        "Tataouine",
        "Tozeur",
        "Tunis",
        "Zaghouan"
    );

    /**
     * Map of country names to their phone prefixes.
     */
    private static final Map<String, String> COUNTRY_PREFIXES = new HashMap<>();
    
    /**
     * Map to hold regions/states/governorates for specific countries.
     */
    private static final Map<String, List<String>> COUNTRY_REGIONS = new HashMap<>();

    static {
        // Initialize country prefixes
        COUNTRY_PREFIXES.put("United States", "+1");
        COUNTRY_PREFIXES.put("Canada", "+1");
        COUNTRY_PREFIXES.put("United Kingdom", "+44");
        COUNTRY_PREFIXES.put("France", "+33");
        COUNTRY_PREFIXES.put("Germany", "+49");
        COUNTRY_PREFIXES.put("Italy", "+39");
        COUNTRY_PREFIXES.put("Spain", "+34");
        COUNTRY_PREFIXES.put("Australia", "+61");
        COUNTRY_PREFIXES.put("Japan", "+81");
        COUNTRY_PREFIXES.put("China", "+86");
        COUNTRY_PREFIXES.put("India", "+91");
        COUNTRY_PREFIXES.put("Brazil", "+55");
        COUNTRY_PREFIXES.put("Mexico", "+52");
        COUNTRY_PREFIXES.put("South Africa", "+27");
        COUNTRY_PREFIXES.put("Egypt", "+20");
        COUNTRY_PREFIXES.put("Tunisia", "+216");
        COUNTRY_PREFIXES.put("Morocco", "+212");
        COUNTRY_PREFIXES.put("Algeria", "+213");
        COUNTRY_PREFIXES.put("Nigeria", "+234");
        COUNTRY_PREFIXES.put("Kenya", "+254");
        
        // Initialize regions for countries
        COUNTRY_REGIONS.put("Tunisia", TUNISIAN_GOVERNORATES);
    };

    /**
     * Returns the list of countries.
     * 
     * @return List of country names
     */
    public static List<String> getCountries() {
        return COUNTRIES;
    }

    /**
     * Checks if a country exists in the predefined list.
     * 
     * @param country The country name to check
     * @return true if the country exists in the list, false otherwise
     */
    public static boolean countryExists(String country) {
        return COUNTRIES.stream()
                .anyMatch(c -> c.equalsIgnoreCase(country));
    }

    /**
     * Returns the default country (first in the list).
     * 
     * @return The default country name
     */
    public static String getDefaultCountry() {
        return COUNTRIES.get(0);
    }

    /**
     * Returns the phone prefix for the specified country.
     * 
     * @param country The country name
     * @return The phone prefix for the country, or an empty string if not found
     */
    public static String getCountryPrefix(String country) {
        return COUNTRY_PREFIXES.getOrDefault(country, "");
    }

    /**
     * Returns a list of all country prefixes.
     * 
     * @return List of country prefixes
     */
    public static List<String> getCountryPrefixes() {
        return List.copyOf(COUNTRY_PREFIXES.values());
    }

    /**
     * Returns a map of country names to their phone prefixes.
     * 
     * @return Map of country names to phone prefixes
     */
    public static Map<String, String> getCountryPrefixMap() {
        return Map.copyOf(COUNTRY_PREFIXES);
    }
    
    /**
     * Checks if a country has regions/governorates defined.
     * 
     * @param country The country name to check
     * @return true if the country has regions defined, false otherwise
     */
    public static boolean hasRegions(String country) {
        return COUNTRY_REGIONS.containsKey(country);
    }
    
    /**
     * Returns the list of regions/governorates for the specified country.
     * 
     * @param country The country name
     * @return List of regions for the country, or null if not found
     */
    public static List<String> getRegions(String country) {
        return COUNTRY_REGIONS.getOrDefault(country, null);
    }
    
    /**
     * Returns the list of Tunisian governorates.
     * 
     * @return List of Tunisian governorates
     */
    public static List<String> getTunisianGovernorates() {
        return TUNISIAN_GOVERNORATES;
    }
    
    /**
     * Returns the default region for a country (first in the list if available).
     * 
     * @param country The country name
     * @return The default region name if available, or null if not found
     */
    public static String getDefaultRegion(String country) {
        List<String> regions = COUNTRY_REGIONS.get(country);
        return (regions != null && !regions.isEmpty()) ? regions.get(0) : null;
    }
}

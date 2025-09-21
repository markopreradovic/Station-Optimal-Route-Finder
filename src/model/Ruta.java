package model;


import java.util.List;

/**
 * Represents a route (Ruta) in a transportation network, consisting of a sequence of departures
 * between a starting and ending city.
 */
public class Ruta {
    private Grad polaziste;
    private Grad odrediste;
    private List<Polazak> polasci;
    private double ukupnaCijena;
    private int ukupnoVrijeme; 
    private int brojPresjedanja;

    /**
     * Constructs a route with the specified details.
     *
     * @param polaziste The starting city of the route.
     * @param odrediste The destination city of the route.
     * @param polasci The list of departures (Polazak) that make up the route.
     * @param ukupnaCijena The total price of the route.
     * @param ukupnoVrijeme The total duration of the route in minutes.
     * @param brojPresjedanja The number of transfers in the route.
     */
    public Ruta(Grad polaziste, Grad odrediste, List<Polazak> polasci, double ukupnaCijena, int ukupnoVrijeme, int brojPresjedanja) {
        this.polaziste = polaziste;
        this.odrediste = odrediste;
        this.polasci = polasci;
        this.ukupnaCijena = ukupnaCijena;
        this.ukupnoVrijeme = ukupnoVrijeme;
        this.brojPresjedanja = brojPresjedanja;
    }

    /**
     * Gets the starting city of the route.
     *
     * @return The starting city (Grad).
     */
    public Grad getPolaziste() { return polaziste; }
    
    /**
     * Gets the destination city of the route.
     *
     * @return The destination city (Grad).
     */
    public Grad getOdrediste() { return odrediste; }
    
    /**
     * Gets the list of departures that make up the route.
     *
     * @return The list of departures (Polazak).
     */
    public List<Polazak> getPolasci() { return polasci; }
    
    /**
     * Gets the total price of the route.
     *
     * @return The total price.
     */
    public double getUkupnaCijena() { return ukupnaCijena; }
    
    /**
     * Gets the total duration of the route in minutes.
     *
     * @return The total duration in minutes.
     */
    public int getUkupnoVrijeme() { return ukupnoVrijeme; }
    
    /**
     * Gets the number of transfers in the route.
     *
     * @return The number of transfers.
     */
    public int getBrojPresjedanja() { return brojPresjedanja; }
}
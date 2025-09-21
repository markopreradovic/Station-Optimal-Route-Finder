package model;

import java.time.LocalTime;
import java.time.Duration;

/**
 * Represents a departure (Polazak) in a transportation network, including details such as
 * departure and arrival times, price, and minimum transfer time.
 */
public class Polazak {
    private String id;
    private Stanica polaziste;
    private Stanica odrediste;
    private LocalTime vrijemePolaska;
    private LocalTime vrijemeDolaska;
    private double cijena;
    private int minimalnoVrijemeCekanja;
    private long totalMinutesAtDeparture;

    /**
     * Constructs a departure with the specified details, initializing total minutes at departure to -1.
     *
     * @param id The unique identifier of the departure.
     * @param polaziste The departure station.
     * @param odrediste The destination station.
     * @param vrijemePolaska The departure time.
     * @param vrijemeDolaska The arrival time.
     * @param cijena The price of the trip.
     * @param minimalnoVrijemeCekanja The minimum transfer time in minutes.
     */
    public Polazak(String id, Stanica polaziste, Stanica odrediste,
                   LocalTime vrijemePolaska, LocalTime vrijemeDolaska,
                   double cijena, int minimalnoVrijemeCekanja) {
        this.id = id;
        this.polaziste = polaziste;
        this.odrediste = odrediste;
        this.vrijemePolaska = vrijemePolaska;
        this.vrijemeDolaska = vrijemeDolaska;
        this.cijena = cijena;
        this.minimalnoVrijemeCekanja = minimalnoVrijemeCekanja;
        this.totalMinutesAtDeparture = -1;
    }

    /**
     * Constructs a departure with the specified details, including total minutes at departure.
     *
     * @param id The unique identifier of the departure.
     * @param polaziste The departure station.
     * @param odrediste The destination station.
     * @param vrijemePolaska The departure time.
     * @param vrijemeDolaska The arrival time.
     * @param cijena The price of the trip.
     * @param minimalnoVrijemeCekanja The minimum transfer time in minutes.
     * @param totalMinutesAtDeparture The total minutes from the start of the route to this departure.
     */
    public Polazak(String id, Stanica polaziste, Stanica odrediste,
                   LocalTime vrijemePolaska, LocalTime vrijemeDolaska,
                   double cijena, int minimalnoVrijemeCekanja, long totalMinutesAtDeparture) {
        this(id, polaziste, odrediste, vrijemePolaska, vrijemeDolaska, cijena, minimalnoVrijemeCekanja);
        this.totalMinutesAtDeparture = totalMinutesAtDeparture;
    }

    /**
     * Gets the total minutes from the start of the route to this departure.
     *
     * @return The total minutes at departure.
     */
    public long getTotalMinutesAtDeparture() {
        return totalMinutesAtDeparture;
    }

    /**
     * Sets the total minutes from the start of the route to this departure.
     *
     * @param totalMinutesAtDeparture The total minutes to set.
     */
    public void setTotalMinutesAtDeparture(long totalMinutesAtDeparture) {
        this.totalMinutesAtDeparture = totalMinutesAtDeparture;
    }

    /**
     * Gets the unique identifier of the departure.
     *
     * @return The departure ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the departure station.
     *
     * @return The departure station (Stanica).
     */
    public Stanica getPolaziste() {
        return polaziste;
    }

    /**
     * Gets the destination station.
     *
     * @return The destination station (Stanica).
     */
    public Stanica getOdrediste() {
        return odrediste;
    }

    /**
     * Gets the departure time.
     *
     * @return The departure time (LocalTime).
     */
    public LocalTime getVrijemePolaska() {
        return vrijemePolaska;
    }

    /**
     * Gets the arrival time.
     *
     * @return The arrival time (LocalTime).
     */
    public LocalTime getVrijemeDolaska() {
        return vrijemeDolaska;
    }

    /**
     * Gets the price of the trip.
     *
     * @return The price of the trip.
     */
    public double getCijena() {
        return cijena;
    }

    /**
     * Gets the minimum transfer time in minutes.
     *
     * @return The minimum transfer time.
     */
    public int getMinimalnoVrijemeCekanja() {
        return minimalnoVrijemeCekanja;
    }

    /**
     * Calculates the duration of the trip in minutes, accounting for overnight trips.
     *
     * @return The duration of the trip in minutes.
     */
    public int getTrajanje() {
        long minutes = Duration.between(vrijemePolaska, vrijemeDolaska).toMinutes();
        if (minutes < 0) {
            minutes += 24 * 60;
        }
        return (int) Math.max(0, minutes);
    }

    /**
     * Returns a string representation of the departure.
     *
     * @return A string containing the departure details.
     */
    @Override
    public String toString() {
        return "Polazak " + id + " (" + polaziste.getId() + " -> " + odrediste.getId() +
               ") " + vrijemePolaska + "-" + vrijemeDolaska +
               ", cijena: " + cijena + ", čekanje: " + minimalnoVrijemeCekanja + "min" +
               (totalMinutesAtDeparture != -1 ? ", Abs. Start: " + totalMinutesAtDeparture + "min" : "");
    }
}

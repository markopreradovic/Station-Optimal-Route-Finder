package model;

import java.util.*;

/**
 * Abstract base class for a station (Stanica) in a transportation network.
 * Provides common functionality for bus and railway stations.
 */
public abstract class Stanica {
	
	protected String id;
    protected Grad grad;
    protected List<Polazak> polasci;

    /**
     * Constructs a station with the specified ID and associated city.
     *
     * @param id The unique identifier of the station.
     * @param grad The city (Grad) where the station is located.
     */
    public Stanica(String id, Grad grad) {
        this.id = id;
        this.grad = grad;
        this.polasci = new ArrayList<>(); 
    }

    /**
     * Gets the unique identifier of the station.
     *
     * @return The station ID.
     */
    public String getId() { return id; }
    
    /**
     * Gets the city where the station is located.
     *
     * @return The city (Grad) associated with the station.
     */
    public Grad getGrad() { return grad; }
    
    /**
     * Gets the list of departures from this station.
     *
     * @return The list of departures (Polazak).
     */
    public List<Polazak> getPolasci() { return polasci; }
   
    /**
     * Adds a departure to the station's list of departures.
     *
     * @param polazak The departure to add.
     */
    public void dodajPolazak(Polazak polazak) {
        polasci.add(polazak);
    }

    /**
     * Returns the type of the station (bus or railway).
     *
     * @return The station type (TipStanice).
     */
    public abstract TipStanice getTip();
	
}

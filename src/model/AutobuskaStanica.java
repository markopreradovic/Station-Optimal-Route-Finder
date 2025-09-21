package model;

/**
 * Represents a bus station, extending the base Stanica class.
 */
public class AutobuskaStanica extends Stanica {
	
	/**
     * Constructs a bus station with the specified ID and associated city.
     *
     * @param id The unique identifier of the station.
     * @param grad The city (Grad) where the station is located.
     */
    public AutobuskaStanica(String id, Grad grad) {
        super(id, grad);
    }
    
    /**
     * Returns the type of the station.
     *
     * @return The station type, which is always AUTOBUSKA (bus).
     */
    @Override
    public TipStanice getTip() {
        return TipStanice.AUTOBUSKA;
    }
}
package model;

/**
 * Represents a railway station, extending the base Stanica class.
 */
public class ZeljeznickaStanica extends Stanica {
	
	/**
     * Constructs a railway station with the specified ID and associated city.
     *
     * @param id The unique identifier of the station.
     * @param grad The city (Grad) where the station is located.
     */
    public ZeljeznickaStanica(String id, Grad grad) {
        super(id, grad);
    }

    /**
     * Returns the type of the station.
     *
     * @return The station type, which is always ZELJEZNICKA (railway).
     */
    @Override
    public TipStanice getTip() {
        return TipStanice.ZELJEZNICKA;
    }
}
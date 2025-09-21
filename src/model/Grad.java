package model;

/**
 * Represents a city (Grad) in a transportation network, which can have a bus and/or railway station.
 */
public class Grad {

	private String naziv;
    private int x; 
    private int y;
    private AutobuskaStanica autobuskaStanica;
    private ZeljeznickaStanica zeljeznickaStanica;

    /**
     * Constructs a city with the specified name and grid coordinates.
     *
     * @param naziv The name of the city.
     * @param x The x-coordinate of the city in the grid.
     * @param y The y-coordinate of the city in the grid.
     */
    public Grad(String naziv, int x, int y) {
        this.naziv = naziv;
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the name of the city.
     *
     * @return The city name.
     */
    public String getNaziv() { return naziv; }
    
    /**
     * Gets the x-coordinate of the city in the grid.
     *
     * @return The x-coordinate.
     */
    public int getX() { return x; }
    
    /**
     * Gets the y-coordinate of the city in the grid.
     *
     * @return The y-coordinate.
     */
    public int getY() { return y; }
    
    /**
     * Gets the bus station associated with the city.
     *
     * @return The bus station (AutobuskaStanica), or null if none exists.
     */
    public AutobuskaStanica getAutobuskaStanica() { return autobuskaStanica; }
    
    /**
     * Gets the railway station associated with the city.
     *
     * @return The railway station (ZeljeznickaStanica), or null if none exists.
     */
    public ZeljeznickaStanica getZeljeznickaStanica() { return zeljeznickaStanica; }
    
    /**
     * Sets the bus station for the city.
     *
     * @param stanica The bus station to set.
     */
    public void setAutobuskaStanica(AutobuskaStanica stanica) { this.autobuskaStanica = stanica; }
    
    /**
     * Sets the railway station for the city.
     *
     * @param stanica The railway station to set.
     */
    public void setZeljeznickaStanica(ZeljeznickaStanica stanica) { this.zeljeznickaStanica = stanica; }
	
}

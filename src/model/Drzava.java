package model;

/**
 * Represents a country (Drzava) as a grid of cities in a transportation network.
 */
public class Drzava {
	
	private int rows;
    private int cols;
    private Grad[][] gradovi;

    /**
     * Constructs a country with a grid of the specified dimensions.
     *
     * @param rows The number of rows in the grid.
     * @param cols The number of columns in the grid.
     */
    public Drzava(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.gradovi = new Grad[rows][cols];
    }

    /**
     * Gets the number of rows in the grid.
     *
     * @return The number of rows.
     */
    public int getRows() { return rows; }
    
    /**
     * Gets the number of columns in the grid.
     *
     * @return The number of columns.
     */
    public int getCols() { return cols; }
    
    /**
     * Gets the city at the specified grid coordinates.
     *
     * @param row The row index.
     * @param col The column index.
     * @return The city (Grad) at the specified coordinates, or null if none exists.
     */
    public Grad getGrad(int row, int col) { return gradovi[row][col]; }
    
    /**
     * Sets the city at the specified grid coordinates.
     *
     * @param row The row index.
     * @param col The column index.
     * @param grad The city (Grad) to set.
     */
    public void setGrad(int row, int col, Grad grad) { gradovi[row][col] = grad; }

	
}

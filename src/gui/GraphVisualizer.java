package gui;

import model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import java.util.HashMap;
import java.util.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.Map;

/**
 * A class for visualizing a transportation network on a canvas using JavaFX.
 * Displays cities, stations, and routes with interactive features like hover effects and route highlighting.
 */
public class GraphVisualizer {
    private Canvas canvas;
    private GraphicsContext gc;
    private int n;
    private int m; 
    private Stanica hoveredStation = null; 
    private Map<Stanica, Point2D> stationPositions = new HashMap<>();
    private Drzava currentDrzava;
    private List<Polazak> highlightedRoute = new ArrayList<>();

    private static final Color BACKGROUND_COLOR = Color.rgb(245, 248, 250);
    private static final Color GRID_COLOR = Color.rgb(220, 225, 230);
    private static final Color TEXT_COLOR = Color.rgb(44, 62, 80);
    private static final Color HOVERED_STATION_COLOR = Color.rgb(255, 215, 0); // Zlatna za hovered
    private static final Color ROUTE_HIGHLIGHT_COLOR = Color.rgb(46, 204, 113); // Zelena za isticanje rute

    private static final Color BUS_GRADIENT_START = Color.rgb(52, 152, 219);
    private static final Color BUS_GRADIENT_END = Color.rgb(41, 128, 185);
    private static final Color TRAIN_GRADIENT_START = Color.rgb(231, 76, 60);
    private static final Color TRAIN_GRADIENT_END = Color.rgb(192, 57, 43);

    private static final Color[] ROUTE_COLORS = { 
    		Color.rgb(255, 99, 71),    // Tomato
            Color.rgb(70, 130, 180),   // Steel Blue
            Color.rgb(255, 165, 0),    // Orange
            Color.rgb(32, 178, 170),   // Light Sea Green
            Color.rgb(255, 20, 147),   // Deep Pink
            Color.rgb(154, 205, 50),   // Yellow Green
            Color.rgb(138, 43, 226),   // Blue Violet
            Color.rgb(255, 140, 0),    // Dark Orange
            Color.rgb(0, 191, 255),    // Deep Sky Blue
            Color.rgb(50, 205, 50),    // Lime Green
            Color.rgb(220, 20, 60),    // Crimson
            Color.rgb(75, 0, 130),     // Indigo
            Color.rgb(255, 215, 0),    // Gold
            Color.rgb(30, 144, 255),   // Dodger Blue
            Color.rgb(255, 69, 0)      // Red Orange
    };

    /**
     * Constructs a GraphVisualizer with the specified canvas and grid dimensions.
     *
     * @param canvas The JavaFX canvas to draw on.
     * @param n The number of rows in the grid.
     * @param m The number of columns in the grid.
     */
    public GraphVisualizer(Canvas canvas, int n, int m) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.n = n;
        this.m = m;
        canvas.setWidth(1400);
        canvas.setHeight(1100);

        setupMouseHandlers();
    }

    /**
     * Sets up mouse event handlers for hover interactions on the canvas.
     */
    private void setupMouseHandlers() {
        canvas.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            Stanica newHoveredStation = findStationAt(mouseX, mouseY);

            if (newHoveredStation != hoveredStation) {
                hoveredStation = newHoveredStation;
                if (currentDrzava != null) {
                    redrawGraph();
                }
            }
        });

        canvas.setOnMouseExited(event -> {
            if (hoveredStation != null) {
                hoveredStation = null;
                if (currentDrzava != null) {
                    redrawGraph();
                }
            }
        });
    }

    /**
     * Finds the station at the specified mouse coordinates, if any.
     *
     * @param x The x-coordinate of the mouse.
     * @param y The y-coordinate of the mouse.
     * @return The station at the coordinates, or null if none is found.
     */
    private Stanica findStationAt(double x, double y) {
        double radius = 22;

        for (Map.Entry<Stanica, Point2D> entry : stationPositions.entrySet()) {
            Point2D pos = entry.getValue();
            double distance = Math.sqrt(Math.pow(x - pos.getX(), 2) + Math.pow(y - pos.getY(), 2));

            if (distance <= radius) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Redraws the entire graph on the canvas.
     */
    private void redrawGraph() {
        drawGraph(currentDrzava);
    }

    /**
     * Draws the transportation network graph based on the provided Drzava object.
     *
     * @param drzava The country object containing city and station data.
     */
    public void drawGraph(Drzava drzava) {
        this.currentDrzava = drzava;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGradientBackground();
        drawGrid();
        positionStations(drzava, stationPositions);
        drawHighlightedRoute();
        if (hoveredStation != null) {
            drawHoverConnections();
        }
        drawNodes(drzava, stationPositions);
    }

    /**
     * Highlights a specific route on the canvas.
     *
     * @param route The list of departures (Polazak) to highlight.
     */
    public void highlightRoute(List<Polazak> route) {
        this.highlightedRoute = route;
        redrawGraph();
    }

    /**
     * Draws the highlighted route on the canvas with a distinct color.
     */
    private void drawHighlightedRoute() {
        if (highlightedRoute == null || highlightedRoute.isEmpty()) return;

        gc.setStroke(ROUTE_HIGHLIGHT_COLOR);
        gc.setLineWidth(5);

        for (int i = 0; i < highlightedRoute.size(); i++) {
            Polazak polazak = highlightedRoute.get(i);
            Point2D startPos = stationPositions.get(polazak.getPolaziste());
            Point2D endPos = stationPositions.get(polazak.getOdrediste());

            if (startPos != null && endPos != null) {
                Point2D adjustedStart = getEdgePoint(startPos, endPos, 22);
                Point2D adjustedEnd = getEdgePoint(endPos, startPos, 22);
                drawSmartLine(adjustedStart, adjustedEnd, i);
                drawSmartArrow(adjustedStart, adjustedEnd, ROUTE_HIGHLIGHT_COLOR);
            }
        }
    }

    /**
     * Draws connections (departures and arrivals) for the currently hovered station.
     */
    private void drawHoverConnections() {
        if (hoveredStation == null)
            return;

        List<Polazak> allDepartures = new ArrayList<>(hoveredStation.getPolasci());

        List<Polazak> allArrivals = new ArrayList<>();
        for (Map.Entry<Stanica, Point2D> entry : stationPositions.entrySet()) {
            Stanica station = entry.getKey();
            if (station != hoveredStation) {
                for (Polazak polazak : station.getPolasci()) {
                    if (polazak.getOdrediste() == hoveredStation) {
                        allArrivals.add(polazak);
                    }
                }
            }
        }

        for (int i = 0; i < allDepartures.size(); i++) {
            Polazak polazak = allDepartures.get(i);
            Stanica dest = polazak.getOdrediste();
            Point2D startPos = stationPositions.get(hoveredStation);
            Point2D destPos = stationPositions.get(dest);

            if (startPos != null && destPos != null) {
                Color routeColor = ROUTE_COLORS[i % ROUTE_COLORS.length];

                gc.setStroke(routeColor);
                gc.setLineWidth(3);

                Point2D adjustedStart = getEdgePoint(startPos, destPos, 22);
                Point2D adjustedEnd = getEdgePoint(destPos, startPos, 22);

                drawSmartLine(adjustedStart, adjustedEnd, i);
                drawSmartArrow(adjustedStart, adjustedEnd, routeColor);
            }
        }

        for (int i = 0; i < allArrivals.size(); i++) {
            Polazak polazak = allArrivals.get(i);
            Point2D startPos = stationPositions.get(polazak.getPolaziste());
            Point2D destPos = stationPositions.get(hoveredStation);

            if (startPos != null && destPos != null) {
                Color routeColor = ROUTE_COLORS[(allDepartures.size() + i) % ROUTE_COLORS.length];

                gc.setStroke(routeColor);
                gc.setLineWidth(3);

                Point2D adjustedStart = getEdgePoint(startPos, destPos, 22);
                Point2D adjustedEnd = getEdgePoint(destPos, startPos, 22);

                drawSmartLine(adjustedStart, adjustedEnd, allDepartures.size() + i);
                drawSmartArrow(adjustedStart, adjustedEnd, routeColor);
            }
        }
    }

    /**
     * Draws a gradient background for the canvas.
     */
    private void drawGradientBackground() {
        for (int i = 0; i < canvas.getHeight(); i += 10) {
            double ratio = i / canvas.getHeight();
            Color gradientColor = interpolateColor(BACKGROUND_COLOR, Color.rgb(235, 245, 255), ratio * 0.3);
            gc.setFill(gradientColor);
            gc.fillRect(0, i, canvas.getWidth(), 10);
        }
    }

    /**
     * Interpolates between two colors based on a ratio.
     *
     * @param color1 The starting color.
     * @param color2 The ending color.
     * @param ratio The interpolation ratio (0.0 to 1.0).
     * @return The interpolated color.
     */
    private Color interpolateColor(Color color1, Color color2, double ratio) {
        double r = color1.getRed() + (color2.getRed() - color1.getRed()) * ratio;
        double g = color1.getGreen() + (color2.getGreen() - color1.getGreen()) * ratio;
        double b = color1.getBlue() + (color2.getBlue() - color1.getBlue()) * ratio;
        return Color.color(r, g, b);
    }

    /**
     * Draws the grid lines and coordinates on the canvas.
     */
    public void drawGrid() {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1);

        double graphWidth = canvas.getWidth();
        double cellWidth = graphWidth / m;
        double cellHeight = canvas.getHeight() / n;

        for (int j = 0; j <= m; j++) {
            double x = j * cellWidth;
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }

        for (int i = 0; i <= n; i++) {
            double y = i * cellHeight;
            gc.strokeLine(0, y, graphWidth, y);
        }

        gc.setFill(Color.rgb(160, 170, 180));
        gc.setFont(Font.font("Arial", FontWeight.LIGHT, 12));
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                gc.fillText(i + "," + j, j * cellWidth + 5, i * cellHeight + 15);
            }
        }
    }

    
    /**
     * Positions stations on the canvas based on their city coordinates.
     *
     * @param drzava The country object containing city data.
     * @param stationPositions The map to store station positions.
     */
    private void positionStations(Drzava drzava, Map<Stanica, Point2D> stationPositions) {
        stationPositions.clear();

        double graphWidth = canvas.getWidth();
        double cellWidth = graphWidth / m;
        double cellHeight = canvas.getHeight() / n;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Grad grad = drzava.getGrad(i, j);
                if (grad != null) {
                    double centerX = j * cellWidth + cellWidth / 2;
                    double centerY = i * cellHeight + cellHeight / 2;

                    Stanica busStation = grad.getAutobuskaStanica();
                    if (busStation != null) {
                        double busX = centerX - cellWidth / 4;
                        double busY = centerY;
                        stationPositions.put(busStation, new Point2D(busX, busY));
                    }

                    Stanica trainStation = grad.getZeljeznickaStanica();
                    if (trainStation != null) {
                        double trainX = centerX + cellWidth / 4;
                        double trainY = centerY;
                        stationPositions.put(trainStation, new Point2D(trainX, trainY));
                    }
                }
            }
        }
    }

    
    /**
     * Draws all nodes (stations) on the canvas with appropriate styling.
     *
     * @param drzava The country object containing city data.
     * @param stationPositions The map of station positions.
     */
    private void drawNodes(Drzava drzava, Map<Stanica, Point2D> stationPositions) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Grad grad = drzava.getGrad(i, j);
                if (grad != null) {
                    Stanica busStation = grad.getAutobuskaStanica();
                    if (busStation != null) {
                        Point2D pos = stationPositions.get(busStation);
                        boolean isHovered = (busStation == hoveredStation);

                        if (isHovered) {
                            drawHoveredNode(pos.getX(), pos.getY(), BUS_GRADIENT_START, BUS_GRADIENT_END,
                                    busStation.getId(), "BUS");
                        } else {
                            drawEnhancedNode(pos.getX(), pos.getY(), BUS_GRADIENT_START, BUS_GRADIENT_END,
                                    busStation.getId(), "BUS");
                        }
                    }

                    Stanica trainStation = grad.getZeljeznickaStanica();
                    if (trainStation != null) {
                        Point2D pos = stationPositions.get(trainStation);
                        boolean isHovered = (trainStation == hoveredStation);

                        if (isHovered) {
                            drawHoveredNode(pos.getX(), pos.getY(), TRAIN_GRADIENT_START, TRAIN_GRADIENT_END,
                                    trainStation.getId(), "VOZ");
                        } else {
                            drawEnhancedNode(pos.getX(), pos.getY(), TRAIN_GRADIENT_START, TRAIN_GRADIENT_END,
                                    trainStation.getId(), "VOZ");
                        }
                    }
                }
            }
        }
    }

    
    /**
     * Draws a station node with enhanced styling, including a gradient fill and label.
     *
     * @param x The x-coordinate of the node.
     * @param y The y-coordinate of the node.
     * @param startColor The starting color of the gradient.
     * @param endColor The ending color of the gradient.
     * @param label The station ID to display.
     * @param type The type of station ("BUS" or "VOZ").
     */
    private void drawEnhancedNode(double x, double y, Color startColor, Color endColor, String label, String type) {
        double radius = 22;

        for (int i = 0; i < radius; i++) {
            double ratio = (double) i / radius;
            Color gradientColor = interpolateColor(startColor, endColor, ratio);
            gc.setFill(gradientColor);
            gc.fillOval(x - radius + i, y - radius + i, (radius - i) * 2, (radius - i) * 2);
        }

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        double textWidth = label.length() * 6.5;
        gc.fillText(label, x - textWidth / 2, y + 2);

        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
        double typeWidth = type.length() * 4;
        gc.fillText(type, x - typeWidth / 2, y - radius - 12);
    }

    
    /**
     * Draws a station node when it is hovered over, with a distinct style.
     *
     * @param x The x-coordinate of the node.
     * @param y The y-coordinate of the node.
     * @param startColor The starting color of the gradient.
     * @param endColor The ending color of the gradient.
     * @param label The station ID to display.
     * @param type The type of station ("BUS" or "VOZ").
     */
    private void drawHoveredNode(double x, double y, Color startColor, Color endColor, String label, String type) {
        double radius = 25;

        for (int i = 0; i < radius; i++) {
            double ratio = (double) i / radius;
            Color gradientColor = interpolateColor(interpolateColor(startColor, HOVERED_STATION_COLOR, 0.3),
                    interpolateColor(endColor, HOVERED_STATION_COLOR, 0.3), ratio);
            gc.setFill(gradientColor);
            gc.fillOval(x - radius + i, y - radius + i, (radius - i) * 2, (radius - i) * 2);
        }

        gc.setStroke(HOVERED_STATION_COLOR);
        gc.setLineWidth(4);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        double textWidth = label.length() * 7;
        gc.fillText(label, x - textWidth / 2, y + 2);

        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        double typeWidth = type.length() * 4.5;
        gc.fillText(type, x - typeWidth / 2, y - radius - 15);
    }

    
    /**
     * Calculates the point on the edge of a node for drawing connections.
     *
     * @param center The center point of the node.
     * @param target The target point to connect to.
     * @param radius The radius of the node.
     * @return The edge point for drawing the connection.
     */
    private Point2D getEdgePoint(Point2D center, Point2D target, double radius) {
        double dx = target.getX() - center.getX();
        double dy = target.getY() - center.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0)
            return center;

        double ratio = radius / distance;
        double edgeX = center.getX() + dx * ratio;
        double edgeY = center.getY() + dy * ratio;

        return new Point2D(edgeX, edgeY);
    }

    
    /**
     * Draws a smart polyline between two points, avoiding overlap with nodes.
     *
     * @param start The starting point of the line.
     * @param end The ending point of the line.
     * @param routeIndex The index of the route for offset calculation.
     */
    private void drawSmartLine(Point2D start, Point2D end, int routeIndex) {
        double startX = start.getX();
        double startY = start.getY();
        double endX = end.getX();
        double endY = end.getY();
        
        double offset = (routeIndex % 3 - 1) * 15;
        
        double dx = endX - startX;
        double dy = endY - startY;
        
        double margin = 30;
        double minX = margin;
        double maxX = canvas.getWidth() - margin;
        double minY = margin;
        double maxY = canvas.getHeight() - margin;
        
        if (Math.abs(dx) > Math.abs(dy)) {
            
            double routeY = (startY + endY) / 2 + offset;
            
            routeY = Math.max(minY, Math.min(maxY, routeY));
            
            gc.strokeLine(startX, startY, startX, routeY);
            gc.strokeLine(startX, routeY, endX, routeY);
            gc.strokeLine(endX, routeY, endX, endY);
            
        } else {
            double routeX = (startX + endX) / 2 + offset;
            routeX = Math.max(minX, Math.min(maxX, routeX));
            
            gc.strokeLine(startX, startY, routeX, startY);
            gc.strokeLine(routeX, startY, routeX, endY);
            gc.strokeLine(routeX, endY, endX, endY);
        }
    }
    
    
    /**
     * Draws an arrowhead at the end of a connection line.
     *
     * @param start The starting point of the line.
     * @param end The ending point of the line.
     * @param color The color of the arrow.
     */
    private void drawSmartArrow(Point2D start, Point2D end, Color color) {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 30)
            return;

        double angle = Math.atan2(dy, dx);
        double arrowSize = 12;

        double arrowDistance = 15;
        double arrowX = end.getX() - arrowDistance * Math.cos(angle);
        double arrowY = end.getY() - arrowDistance * Math.sin(angle);

        gc.setFill(color);
        gc.setStroke(color);
        gc.setLineWidth(2);

        double arrowAngle = Math.PI / 5;
        double[] xPoints = { arrowX, arrowX - arrowSize * Math.cos(angle - arrowAngle),
                arrowX - arrowSize * Math.cos(angle + arrowAngle) };
        double[] yPoints = { arrowY, arrowY - arrowSize * Math.sin(angle - arrowAngle),
                arrowY - arrowSize * Math.sin(angle + arrowAngle) };

        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);
    }
}
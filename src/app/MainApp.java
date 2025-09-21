package app;

import java.time.LocalTime; // Dodati import
import java.time.format.DateTimeFormatter;
import java.util.List;

import algorithm.KShortestPathsFinder;
import data.JsonParser;
import gui.GraphVisualizer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import model.*;
import service.IzdavanjeRacuna;


/**
 * Main application class for the Transport Route Finder, a JavaFX application
 * that allows users to find optimal transportation routes between cities based
 * on specified criteria (time, price, or number of transfers).
 */
public class MainApp extends Application {
	
    private Drzava drzava;
    private GraphVisualizer graphVisualizer;
    private ComboBox<String> startCityCombo;
    private ComboBox<String> endCityCombo;
    private ComboBox<String> criterionCombo;
    private StackPane canvasContainer;
    private TableView<RouteSegment> routeTable;
    private Button additionalRoutesButton;
    private Button buyTicketButton;
    private VBox tableContainer;
    private List<Ruta> additionalRoutes;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    
    /**
     * Initializes and starts the JavaFX application, setting up the UI and loading
     * transportation data from a JSON file.
     *
     * @param primaryStage The primary stage for the JavaFX application.
     */
    @Override
    public void start(Stage primaryStage) {
        JsonParser parser = new JsonParser();
        try {
            drzava = parser.parse("transport_data.json");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setHeaderText(null);
            alert.setContentText("Neuspešno učitavanje JSON fajla: " + e.getMessage());
            alert.showAndWait();
            return;
        }

        HBox root = new HBox();
        root.setStyle("-fx-background-color: #f5f8fa;");
        root.setPadding(new Insets(0));
        root.setSpacing(0);

        Canvas canvas = new Canvas(1100, 1100);
        graphVisualizer = new GraphVisualizer(canvas, drzava.getRows(), drzava.getCols());

        canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-background-color: white; -fx-border-color: #dce1e6; -fx-border-width: 1;");
        canvasContainer.setPadding(new Insets(0));

        ScrollPane scrollPane = new ScrollPane(canvasContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #f5f8fa;");
        scrollPane.setPadding(new Insets(0));

        VBox controlPanel = createControlPanel();

        HBox mainLayout = new HBox();
        mainLayout.setPadding(new Insets(0));
        mainLayout.setSpacing(0);

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        controlPanel.setMinWidth(500);
        controlPanel.setMaxWidth(500);

        mainLayout.getChildren().addAll(scrollPane, controlPanel);
        root.getChildren().add(mainLayout);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getRoot().setStyle("-fx-padding: 0;");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Transport Route Finder");
        primaryStage.setMaximized(true);
        primaryStage.show();

        graphVisualizer.drawGraph(drzava);
    }

    /**
     * Creates the control panel containing UI elements for selecting cities,
     * criteria, and initiating route searches.
     *
     * @return A VBox containing the control panel components.
     */
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(20));
        controlPanel.setMaxWidth(240);
        controlPanel.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95); " +
            "-fx-border-color: #b4c4d4; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 15; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);"
        );

        Label titleLabel = new Label("KONTROLE");
        titleLabel.setStyle(
            "-fx-font-family: Arial; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-text-fill: #2c3e50;"
        );
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        
        String racuni = IzdavanjeRacuna.pregledajRacune();
        Label racuniLabel = new Label(racuni);
        racuniLabel.setTextAlignment(TextAlignment.CENTER);
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #c8d6e5;");

        startCityCombo = createStyledComboBox();
        endCityCombo = createStyledComboBox();
        populateCityCombos();

        criterionCombo = createStyledComboBox();
        criterionCombo.getItems().addAll("Najkraće vrijeme", "Najniža cijena", "Najmanje presjedanja");
        criterionCombo.setValue("Najkraće vrijeme");

        Label startLabel = createStyledLabel("Početni grad:");
        Label endLabel = createStyledLabel("Odredišni grad:");
        Label criterionLabel = createStyledLabel("Kriterijum:");

        Button searchButton = createStyledButton("Pretraži", "#3498db", "#2980b9");
        searchButton.setOnAction(event -> searchRoute());

        

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(searchButton);
        buttonBox.setAlignment(Pos.CENTER);

        createRouteTable();

        additionalRoutesButton = createStyledButton("Prikaz dodatnih ruta", "#27ae60", "#229954");
        additionalRoutesButton.setVisible(false);
        additionalRoutesButton.setOnAction(event -> showAdditionalRoutes());

        buyTicketButton = createStyledButton("Kupovina karte", "#f39c12", "#e67e22");
        buyTicketButton.setVisible(false);
        buyTicketButton.setOnAction(event -> buyTicket());

        HBox tableButtonsBox = new HBox(10);
        tableButtonsBox.getChildren().addAll(additionalRoutesButton, buyTicketButton);
        tableButtonsBox.setAlignment(Pos.CENTER);

        tableContainer = new VBox(10);
        tableContainer.getChildren().addAll(routeTable, tableButtonsBox);
        tableContainer.setVisible(false);

        controlPanel.getChildren().addAll(
            titleLabel,
            racuniLabel,
            separator,
            startLabel, startCityCombo,
            endLabel, endCityCombo,
            criterionLabel, criterionCombo,
            buttonBox,
            tableContainer
        );
        VBox.setMargin(controlPanel, new Insets(10, 10, 10, 10));
        return controlPanel;
    }

    /**
     * Creates and configures the table for displaying route segments.
     */
    private void createRouteTable() {
        routeTable = new TableView<>();
        routeTable.setPrefHeight(150);
        routeTable.setMaxHeight(150);
        
        routeTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<RouteSegment, String> departureCol = new TableColumn<>("Polazak");
        departureCol.setCellValueFactory(new PropertyValueFactory<>("departure"));

        TableColumn<RouteSegment, String> arrivalCol = new TableColumn<>("Dolazak");
        arrivalCol.setCellValueFactory(new PropertyValueFactory<>("arrival"));

        TableColumn<RouteSegment, String> typeCol = new TableColumn<>("Tip");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<RouteSegment, String> priceCol = new TableColumn<>("Cijena");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Fixed: Use @SafeVarargs alternative by adding columns individually
        routeTable.getColumns().add(departureCol);
        routeTable.getColumns().add(arrivalCol);
        routeTable.getColumns().add(typeCol);
        routeTable.getColumns().add(priceCol);

        routeTable.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #b4c4d4; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );

        departureCol.setStyle("-fx-alignment: CENTER;");
        arrivalCol.setStyle("-fx-alignment: CENTER;");
        typeCol.setStyle("-fx-alignment: CENTER;");
        priceCol.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Creates a styled ComboBox for selecting cities or criteria.
     *
     * @return A styled ComboBox with hover effects.
     */
    private ComboBox<String> createStyledComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #b4c4d4; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 5 10 5 10;"
        );

        combo.setOnMouseEntered(event -> combo.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #3498db; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 5 10 5 10;"
        ));

        combo.setOnMouseExited(event -> combo.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #b4c4d4; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 5 10 5 10;"
        ));

        return combo;
    }

    /**
     * Creates a styled Label for the control panel.
     *
     * @param text The text to display in the Label.
     * @return A styled Label.
     */
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
            "-fx-font-family: Arial; " +
            "-fx-font-weight: normal; " +
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #2c3e50;"
        );
        return label;
    }

    /**
     * Creates a styled Button with hover effects.
     *
     * @param text The text to display on the Button.
     * @param bgColor The background color of the Button.
     * @param hoverColor The background color when the Button is hovered.
     * @return A styled Button.
     */
    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 11px; " +
            "-fx-padding: 8 15 8 15; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(event -> button.setStyle(
            "-fx-background-color: " + hoverColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 11px; " +
            "-fx-padding: 8 15 8 15; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand; " +
            "-fx-scale-y: 1.05;"
        ));

        button.setOnMouseExited(event -> button.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 11px; " +
            "-fx-padding: 8 15 8 15; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand; " +
            "-fx-scale-y: 1.0;"
        ));

        return button;
    }

    /**
     * Populates the city ComboBoxes with city names from the Drzava object.
     */
    private void populateCityCombos() {
        for (int i = 0; i < drzava.getRows(); i++) {
            for (int j = 0; j < drzava.getCols(); j++) {
                Grad grad = drzava.getGrad(i, j);
                if (grad != null) {
                    String cityName = grad.getNaziv();
                    startCityCombo.getItems().add(cityName);
                    endCityCombo.getItems().add(cityName);
                }
            }
        }
    }

    /**
     * Searches for and displays the optimal route based on user-selected cities and criterion.
     */
    private void searchRoute() {
        String startCity = startCityCombo.getValue();
        String endCity = endCityCombo.getValue();
        String criterion = criterionCombo.getValue();

        if (startCity == null || endCity == null || criterion == null) {
            showAlert("Greška", "Izaberite početni grad, odredišni grad i kriterijum.", Alert.AlertType.WARNING);
            return;
        }

        if (startCity.equals(endCity)) {
            showAlert("Greška", "Početni i odredišni grad ne mogu biti isti.", Alert.AlertType.WARNING);
            return;
        }

        Grad startGrad = null, endGrad = null;
        for (int i = 0; i < drzava.getRows(); i++) {
            for (int j = 0; j < drzava.getCols(); j++) {
                Grad grad = drzava.getGrad(i, j);
                if (grad != null && grad.getNaziv().equals(startCity)) {
                    startGrad = grad;
                }
                if (grad != null && grad.getNaziv().equals(endCity)) {
                    endGrad = grad;
                }
            }
        }

        if (startGrad == null || endGrad == null) {
            showAlert("Greška", "Nepostojeći gradovi.", Alert.AlertType.ERROR);
            return;
        }

        KShortestPathsFinder routeFinder = new KShortestPathsFinder(drzava);
        String criterionKey;
        switch (criterion) {
            case "Najkraće vrijeme":
                criterionKey = "time";
                break;
            case "Najniža cijena":
                criterionKey = "price";
                break;
            case "Najmanje presjedanja":
                criterionKey = "transfers";
                break;
            default:
                criterionKey = "time";
        }

        additionalRoutes = routeFinder.findKShortestPaths(startGrad, endGrad, criterionKey, 5);
        Ruta optimalRoute = additionalRoutes.isEmpty() ? null : additionalRoutes.get(0);

        if (optimalRoute == null || optimalRoute.getPolasci().isEmpty()) {
            showAlert("Greška", "Nema dostupne rute između " + startCity + " i " + endCity + ".", Alert.AlertType.WARNING);
            return;
        }

        if (optimalRoute.getPolasci().stream().noneMatch(p -> !p.getId().startsWith("transfer_"))) {
            showAlert("Greška", "Pronađena ruta sadrži samo transfere.", Alert.AlertType.WARNING);
            return;
        }

        routeTable.getItems().clear();
        for (Polazak polazak : optimalRoute.getPolasci()) {
            // Koristite totalMinutesAtDeparture za precizan prikaz vremena
            LocalTime actualDepartureTime = convertMinutesToLocalTime(polazak.getTotalMinutesAtDeparture());
            LocalTime actualArrivalTime = convertMinutesToLocalTime(polazak.getTotalMinutesAtDeparture() + polazak.getTrajanje());

            if (polazak.getId().startsWith("transfer_")) {
            	String departure = String.format("%s (%s)", 
                        polazak.getPolaziste().getId(), 
                        actualDepartureTime.format(TIME_FORMATTER));
                    String arrival = String.format("%s (%s)", 
                        polazak.getOdrediste().getId(), 
                        actualArrivalTime.format(TIME_FORMATTER));
                String type = "Transfer";
                String price = "0.00";
                routeTable.getItems().add(new RouteSegment(departure, arrival, type, price));
            } else {
                String departure = String.format("%s (%s)", polazak.getPolaziste().getId(), 
                    actualDepartureTime.format(TIME_FORMATTER));
                String arrival = String.format("%s (%s)", polazak.getOdrediste().getId(), 
                    actualArrivalTime.format(TIME_FORMATTER));
                String type = polazak.getPolaziste().getTip() == TipStanice.AUTOBUSKA ? "Autobus" : "Voz";
                String price = String.format("%.2f", polazak.getCijena());
                routeTable.getItems().add(new RouteSegment(departure, arrival, type, price));
            }
        }

        graphVisualizer.drawGraph(drzava);
        graphVisualizer.highlightRoute(optimalRoute.getPolasci());

        String summary = String.format("Ukupno: %d min, %.2f novčanih jedinica, %d presjedanja",
            optimalRoute.getUkupnoVrijeme(), optimalRoute.getUkupnaCijena(), optimalRoute.getBrojPresjedanja());
        showAlert("Ruta pronađena", summary, Alert.AlertType.INFORMATION);

        tableContainer.setVisible(true);
        additionalRoutesButton.setVisible(true);
        buyTicketButton.setVisible(true);
    }

    
    /**
     * Displays a window with the top 5 alternative routes based on the selected criterion.
     */
    private void showAdditionalRoutes() {
        if (additionalRoutes == null || additionalRoutes.isEmpty()) {
            showAlert("Greška", "Nema dostupnih ruta za prikaz.", Alert.AlertType.WARNING);
            return;
        }

        Stage additionalRoutesStage = new Stage();
        additionalRoutesStage.setTitle("Top 5 Ruta - " + criterionCombo.getValue());
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f5f8fa;");

        Label titleLabel = new Label("Top 5 Najboljih Ruta");
        titleLabel.setStyle(
            "-fx-font-family: Arial; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 18px; " +
            "-fx-text-fill: #2c3e50;"
        );
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label criterionLabel = new Label("Kriterijum: " + criterionCombo.getValue());
        criterionLabel.setStyle(
            "-fx-font-family: Arial; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #34495e;"
        );
        criterionLabel.setAlignment(Pos.CENTER);
        criterionLabel.setMaxWidth(Double.MAX_VALUE);

        layout.getChildren().addAll(titleLabel, criterionLabel, new Separator());

        for (int i = 0; i < Math.min(5, additionalRoutes.size()); i++) {
            Ruta ruta = additionalRoutes.get(i);
            VBox routeBox = createRouteDisplayBox(ruta, i + 1, additionalRoutesStage);
            layout.getChildren().add(routeBox);
        }

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f8fa;");
        
        Scene scene = new Scene(scrollPane, 750, 600);
        additionalRoutesStage.setScene(scene);
        additionalRoutesStage.show();
    }
    
    /**
     * Creates a display box for a single route in the additional routes window.
     *
     * @param ruta The route to display.
     * @param routeNumber The route number (1 to 5).
     * @param parentStage The parent stage to close when buying a ticket.
     * @return A VBox containing the route information and controls.
     */
    private VBox createRouteDisplayBox(Ruta ruta, int routeNumber, Stage parentStage) {
        VBox routeBox = new VBox(10);
        routeBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #bdc3c7; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(15);

        Label routeNumberLabel = new Label("RUTA " + routeNumber);
        routeNumberLabel.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 5 10 5 10; " +
            "-fx-background-radius: 15;"
        );

        String criterion = criterionCombo.getValue();
        String primaryInfo = "";
        String secondaryInfo = "";
        
        switch (criterion) {
            case "Najkraće vrijeme":
                primaryInfo = String.format("%d min", ruta.getUkupnoVrijeme());
                secondaryInfo = String.format("%.2f NJ | %d presjedanja", 
                    ruta.getUkupnaCijena(), ruta.getBrojPresjedanja());
                break;
            case "Najniža cijena":
                primaryInfo = String.format("%.2f NJ", ruta.getUkupnaCijena());
                secondaryInfo = String.format("%d min | %d presjedanja", 
                    ruta.getUkupnoVrijeme(), ruta.getBrojPresjedanja());
                break;
            case "Najmanje presjedanja":
                primaryInfo = String.format("%d presjedanja", ruta.getBrojPresjedanja());
                secondaryInfo = String.format("%d min | %.2f NJ", 
                    ruta.getUkupnoVrijeme(), ruta.getUkupnaCijena());
                break;
        }

        Label primaryInfoLabel = new Label(primaryInfo);
        primaryInfoLabel.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-text-fill: #27ae60;"
        );

        Label secondaryInfoLabel = new Label(secondaryInfo);
        secondaryInfoLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #7f8c8d;"
        );

        VBox infoBox = new VBox(2);
        infoBox.getChildren().addAll(primaryInfoLabel, secondaryInfoLabel);

        headerBox.getChildren().addAll(routeNumberLabel, infoBox);

        TableView<RouteSegment> routeTable = new TableView<>();
        routeTable.setPrefHeight(120);
        routeTable.setMaxHeight(120);
        // Fixed: Use modern TableView column resize policy
        routeTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<RouteSegment, String> departureCol = new TableColumn<>("Polazak");
        departureCol.setCellValueFactory(new PropertyValueFactory<>("departure"));
        departureCol.setPrefWidth(200);

        TableColumn<RouteSegment, String> arrivalCol = new TableColumn<>("Dolazak");
        arrivalCol.setCellValueFactory(new PropertyValueFactory<>("arrival"));
        arrivalCol.setPrefWidth(200);

        TableColumn<RouteSegment, String> typeCol = new TableColumn<>("Tip");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);

        TableColumn<RouteSegment, String> priceCol = new TableColumn<>("Cijena");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        // Fixed: Use @SafeVarargs alternative by adding columns individually
        routeTable.getColumns().add(departureCol);
        routeTable.getColumns().add(arrivalCol);
        routeTable.getColumns().add(typeCol);
        routeTable.getColumns().add(priceCol);

        for (Polazak polazak : ruta.getPolasci()) {
            // Koristite totalMinutesAtDeparture za precizan prikaz vremena
            LocalTime actualDepartureTime = convertMinutesToLocalTime(polazak.getTotalMinutesAtDeparture());
            LocalTime actualArrivalTime = convertMinutesToLocalTime(polazak.getTotalMinutesAtDeparture() + polazak.getTrajanje());

            if (polazak.getId().startsWith("transfer_")) {
            	String departure = String.format("%s (%s)", 
            		    polazak.getPolaziste().getId(), 
            		    actualDepartureTime.format(TIME_FORMATTER));
            		String arrival = String.format("%s (%s)", 
            		    polazak.getOdrediste().getId(), 
            		    actualArrivalTime.format(TIME_FORMATTER));
                String type = "Transfer";
                String price = "0.00 NJ";
                routeTable.getItems().add(new RouteSegment(departure, arrival, type, price));
            } else {
                String departure = String.format("%s (%s)", polazak.getPolaziste().getId(), 
                    actualDepartureTime.format(TIME_FORMATTER));
                String arrival = String.format("%s (%s)", polazak.getOdrediste().getId(), 
                    actualArrivalTime.format(TIME_FORMATTER));
                String type = polazak.getPolaziste().getTip() == TipStanice.AUTOBUSKA ? "Autobus" : "Voz";
                String price = String.format("%.2f NJ", polazak.getCijena());
                routeTable.getItems().add(new RouteSegment(departure, arrival, type, price));
            }
        }

        routeTable.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #e9ecef; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );

        Button buyButton = createStyledButton("Kupi ovu kartu", "#e74c3c", "#c0392b");
        buyButton.setMaxWidth(200);
        buyButton.setOnAction(event -> {
            buyTicket(ruta);
            parentStage.close();
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(buyButton);

        routeBox.getChildren().addAll(headerBox, routeTable, buttonBox);
        return routeBox;
    }

    /**
     * Initiates the purchase of a ticket for the optimal route.
     */
    private void buyTicket() {
        buyTicket(additionalRoutes.get(0));
    }

    /**
     * Generates a receipt for the selected route.
     *
     * @param ruta The route for which to generate a receipt.
     */
    private void buyTicket(Ruta ruta) {
    	IzdavanjeRacuna.generisiRacun(ruta);
    }

    /**
     * Converts a total number of minutes since the start into a LocalTime object,
     * representing the time of day.
     *
     * @param totalMinutes The total minutes since the start.
     * @return The corresponding LocalTime object.
     */
    private LocalTime convertMinutesToLocalTime(long totalMinutes) {
        long minutesInDay = totalMinutes % 1440; // Minuta u danu (0-1439)
        int hour = (int) (minutesInDay / 60);
        int minute = (int) (minutesInDay % 60);
        return LocalTime.of(hour, minute);
    }

    
    /**
     * Displays an alert with the specified title, message, and type.
     *
     * @param title The title of the alert.
     * @param message The message to display.
     * @param type The type of alert (e.g., ERROR, WARNING, INFORMATION).
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #b4c4d4; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10;"
        );

        alert.showAndWait();
    }

    
    /**
     * Inner class representing a segment of a route for display in a TableView.
     */
    public static class RouteSegment {
        private String departure;
        private String arrival;
        private String type;
        private String price;

        /**
         * Constructs a RouteSegment with the specified attributes.
         *
         * @param departure The departure station and time.
         * @param arrival The arrival station and time.
         * @param type The type of transport (e.g., Autobus, Voz, Transfer).
         * @param price The price of the segment.
         */
        public RouteSegment(String departure, String arrival, String type, String price) {
            this.departure = departure;
            this.arrival = arrival;
            this.type = type;
            this.price = price;
        }

        /**
         * Gets the departure station and time.
         *
         * @return The departure string.
         */
        public String getDeparture() { 
            return departure;
        }
        
        /**
         * Gets the arrival station and time.
         *
         * @return The arrival string.
         */
        public String getArrival() { 
            return arrival;
        }
        
        /**
         * Gets the type of transport.
         *
         * @return The transport type.
         */
        public String getType() { 
            return type;
        }
        
        /**
         * Gets the price of the segment.
         *
         * @return The price string.
         */
        public String getPrice() { 
            return price;
        }
    }

    
    /**
     * The main entry point for the JavaFX application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

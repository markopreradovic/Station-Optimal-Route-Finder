package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.Alert;
import model.Polazak;
import model.Ruta;

/**
 * A utility class for generating and reviewing receipts (računi) for transportation routes.
 */
public class IzdavanjeRacuna {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Generates a receipt for a given route and saves it to a text file.
     *
     * @param ruta The route (Ruta) for which to generate the receipt.
     */
    public static void generisiRacun(Ruta ruta) {
        if (ruta == null || ruta.getPolasci().isEmpty()) {
            prikaziPoruku("Greška", "Nevalidna ruta za izdavanje računa.", Alert.AlertType.ERROR);
            return;
        }

        File folder = new File("racuni");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "racuni/racun_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("========== RAČUN ==========\n");
            writer.write("Relacija: " + ruta.getPolaziste().getNaziv() + " → " + ruta.getOdrediste().getNaziv() + "\n");
            writer.write("Datum i vrijeme izdavanja: " + LocalDateTime.now().format(DATE_TIME_FORMAT) + "\n\n");

            writer.write("DETALJI VOŽNJI:\n");

            for (Polazak p : ruta.getPolasci()) {
                writer.write("- " + p.getPolaziste().getId() + " → " + p.getOdrediste().getId() + "\n");
                writer.write("  Vrijeme: " + p.getVrijemePolaska() + " - " + p.getVrijemeDolaska() + "\n");
                writer.write(String.format("  Cijena: %.2f NJ\n", p.getCijena()));
                writer.write("\n");
            }

            writer.write("============================\n");
            writer.write("UKUPNA_CIJENA=" + String.format("%.2f", ruta.getUkupnaCijena()) + "\n");  // Ova linija za parsiranje
            writer.write("============================\n");

            prikaziPoruku("Račun izdat", "Račun je uspješno sačuvan u fajl:\n" + fileName, Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            prikaziPoruku("Greška", "Greška prilikom upisa računa:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Reviews all saved receipts and calculates the total number and revenue.
     *
     * @return A string summarizing the total number of receipts and total revenue.
     */
    public static String pregledajRacune() {
        File folder = new File("racuni");
        if (!folder.exists() || !folder.isDirectory()) {
            return "Folder sa računima ne postoji.";
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            return "Nema izdatih računa.";
        }

        int brojRacuna = 0;
        double ukupniPrihod = 0.0;

        for (File f : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("UKUPNA_CIJENA=")) {
                        String vrednost = line.substring("UKUPNA_CIJENA=".length()).trim();
                        try {
                            double cijena = Double.parseDouble(vrednost);
                            ukupniPrihod += cijena;
                            brojRacuna++;
                        } catch (NumberFormatException e) {
                            
                        }
                        break; 
                    }
                }
            } catch (IOException e) {
                
            }
        }

        return String.format("Ukupno izdatih racuna: %d Ukupan prihod: %.2f NJ", brojRacuna, ukupniPrihod);
    }

    /**
     * Displays a message dialog with the specified title, message, and alert type.
     *
     * @param naslov The title of the dialog.
     * @param poruka The message to display.
     * @param tip The type of alert (e.g., ERROR, INFORMATION).
     */
    private static void prikaziPoruku(String naslov, String poruka, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}

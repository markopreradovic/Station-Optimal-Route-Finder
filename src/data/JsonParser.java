package data;

import model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * A class for parsing JSON data into a Drzava domain model, representing a transportation network.
 */
public class JsonParser {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    
    /**
     * Internal class to represent the structure of the JSON transport data.
     */
    private static class JsonTransportData {
        String[][] countryMap;
        JsonStation[] stations;
        JsonDeparture[] departures;
    }

    
    /**
     * Internal class to represent a station in the JSON data.
     */
    private static class JsonStation {
        String city;
        String busStation;
        String trainStation;
    }

    
    /**
     * Internal class to represent a departure in the JSON data.
     */
    private static class JsonDeparture {
        String type;
        String from;
        String to;
        String departureTime;
        int duration;
        int price;
        int minTransferTime;
    }

    
    /**
     * Parses a JSON file into a Drzava object.
     *
     * @param filePath The path to the JSON file.
     * @return A Drzava object representing the parsed transportation network.
     * @throws RuntimeException if there is an error parsing the JSON file.
     */
    public Drzava parse(String filePath) {
        Gson gson = new GsonBuilder().create();
        try (FileReader reader = new FileReader(filePath)) {
            JsonTransportData jsonData = gson.fromJson(reader, JsonTransportData.class);
            return convertToDomainModel(jsonData);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri parsiranju JSON fajla", e);
        }
    }

    /**
     * Converts the JSON transport data into a domain model (Drzava).
     *
     * @param jsonData The JSON transport data to convert.
     * @return A Drzava object populated with cities, stations, and departures.
     */
    private Drzava convertToDomainModel(JsonTransportData jsonData) {
        Drzava drzava = new Drzava(jsonData.countryMap.length, jsonData.countryMap[0].length);
        
        Map<String, Stanica> staniceMap = new HashMap<>();
        
        for (JsonStation jsonStation : jsonData.stations) {
            String[] coordinates = jsonStation.city.split("_");
            int x = Integer.parseInt(coordinates[1]);
            int y = Integer.parseInt(coordinates[2]);
            Grad grad = new Grad(jsonStation.city, x, y);
            drzava.setGrad(x, y, grad);
            
            AutobuskaStanica autobuska = new AutobuskaStanica(jsonStation.busStation, grad);
            grad.setAutobuskaStanica(autobuska);
            staniceMap.put(jsonStation.busStation, autobuska);
            
            ZeljeznickaStanica zeljeznicka = new ZeljeznickaStanica(jsonStation.trainStation, grad);
            grad.setZeljeznickaStanica(zeljeznicka);
            staniceMap.put(jsonStation.trainStation, zeljeznicka);
        }
        
        for (JsonDeparture jsonDeparture : jsonData.departures) {
            Stanica polaziste = staniceMap.get(jsonDeparture.from);
            Stanica odrediste = staniceMap.get(
                jsonDeparture.type.equals("autobus") ? 
                "A_" + jsonDeparture.to.split("_")[1] + "_" + jsonDeparture.to.split("_")[2] :
                "Z_" + jsonDeparture.to.split("_")[1] + "_" + jsonDeparture.to.split("_")[2]
            );
            
            LocalTime vrijemePolaska = LocalTime.parse(jsonDeparture.departureTime, TIME_FORMATTER);
            LocalTime vrijemeDolaska = vrijemePolaska.plusMinutes(jsonDeparture.duration);
            
            Polazak polazak = new Polazak(
                jsonDeparture.from + "_to_" + odrediste.getId(),
                polaziste,
                odrediste,
                vrijemePolaska,
                vrijemeDolaska,
                jsonDeparture.price,
                jsonDeparture.minTransferTime
            );
            
            polaziste.dodajPolazak(polazak);
        }
        
        return drzava;
    }
    
    
    
    
}
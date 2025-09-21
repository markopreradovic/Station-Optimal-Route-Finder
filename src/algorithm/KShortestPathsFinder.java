package algorithm;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import model.*;


//Napomena: Za transfer izmedju stanica u gradu koristi minimalno vrijeme cekanja


/**
 * Class that implements the K shortest paths algorithm to find the best routes between cities
 * based on specified criteria (time, price, or number of transfers).
 * Uses a graph representation of stations and their departures to compute optimal paths.
 */
public class KShortestPathsFinder {
    private Drzava drzava;
    private Map<Stanica, Map<Stanica, List<Polazak>>> graph;

    
    /**
     * Constructs a KShortestPathsFinder with the given country data.
     * Initializes the graph representation of the transportation network.
     *
     * @param drzava The country object containing city and station information.
     */
    public KShortestPathsFinder(Drzava drzava) {
        this.drzava = drzava;
        buildGraph();
    }

    
    /**
     * Builds the graph representation of the transportation network by adding stations
     * and their departures, including transfers between stations in the same city.
     */
    private void buildGraph() {
        graph = new HashMap<>();
        for (int i = 0; i < drzava.getRows(); i++) {
            for (int j = 0; j < drzava.getCols(); j++) {
                Grad grad = drzava.getGrad(i, j);
                if (grad != null) {
                    addStationToGraph(grad.getAutobuskaStanica());
                    addStationToGraph(grad.getZeljeznickaStanica());
                }
            }
        }
    }

    
    /**
     * Adds a station and its departures to the graph, including a transfer to the other
     * station type (bus or train) in the same city if applicable.
     *
     * @param station The station to add to the graph.
     */
    private void addStationToGraph(Stanica station) {
        if (station == null) return;
        Map<Stanica, List<Polazak>> destinations = new HashMap<>();
        
        for (Polazak polazak : station.getPolasci()) {
            if (polazak.getTrajanje() >= 0) {
                destinations.computeIfAbsent(polazak.getOdrediste(), k -> new ArrayList<>()).add(polazak);
            }
        }
        
        Grad grad = station.getGrad();
        Stanica otherStation = (station.getTip() == TipStanice.AUTOBUSKA) ? 
            grad.getZeljeznickaStanica() : grad.getAutobuskaStanica();
        
        if (otherStation != null) {
            Polazak transferPolazak = new Polazak(
                "transfer_" + station.getId() + "_to_" + otherStation.getId(),
                station,
                otherStation,
                LocalTime.of(0, 0), 
                LocalTime.of(0, 0), 
                0.0,
                0
            );
            destinations.computeIfAbsent(otherStation, k -> new ArrayList<>()).add(transferPolazak);
        }
        
        graph.put(station, destinations);
    }

    
    /**
     * Creates a unique signature for a path based on its segments to avoid duplicate routes.
     *
     * @param segments The list of departure segments in the path.
     * @return A string representing the unique signature of the path.
     */
    private String createPathSignature(List<Polazak> segments) {
        StringBuilder signature = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            Polazak segment = segments.get(i);
            signature.append(i).append(":")
                     .append(segment.getPolaziste().getId())
                     .append("->")
                     .append(segment.getOdrediste().getId())
                     .append("_")
                     .append(segment.getId())
                     .append("_")
                     .append(segment.getVrijemePolaska()) 
                     .append(";");
        }
        return signature.toString();
    }

    
    /**
     * Finds the next available departure time for a given departure, considering the
     * current arrival time and minimum waiting time.
     *
     * @param currentArrivalTime The current arrival time in minutes since the start.
     * @param polazak The departure to evaluate.
     * @return The next available departure time in minutes since the start.
     */
    private long findNextAvailableDeparture(long currentArrivalTime, Polazak polazak) {
        long departureMinutes = polazak.getVrijemePolaska().toSecondOfDay() / 60;
        long minWaitTime = polazak.getMinimalnoVrijemeCekanja();
        long earliestBoardingTime = currentArrivalTime + minWaitTime;
        long currentDay = currentArrivalTime / 1440;
        long todayDeparture = currentDay * 1440 + departureMinutes;
        
        if (todayDeparture >= earliestBoardingTime) {
            return todayDeparture;
        }
        return (currentDay + 1) * 1440 + departureMinutes;
    }

    /**
     * Retrieves the minimum transfer wait time from the last regular (non-transfer) departure
     * in the path segments.
     *
     * @param segments The list of departure segments in the path.
     * @return The minimum wait time for a transfer, or 0 if no regular departures exist.
     */
    private int getTransferWaitTimeFromLastSegment(List<Polazak> segments) {
        if (segments.isEmpty()) {
            return 0; 
        }
        
        for (int i = segments.size() - 1; i >= 0; i--) {
            Polazak segment = segments.get(i);
            if (!segment.getId().startsWith("transfer_")) {
                return segment.getMinimalnoVrijemeCekanja();
            }
        }
        
        return 0; 
    }

    
    /**
     * Finds the k shortest paths between two cities based on the specified criterion.
     *
     * @param startGrad The starting city.
     * @param endGrad The destination city.
     * @param criterion The optimization criterion ("time", "price", or "transfers").
     * @param k The number of shortest paths to return.
     * @return A list of up to k routes, sorted by the specified criterion.
     */
    public List<Ruta> findKShortestPaths(Grad startGrad, Grad endGrad, String criterion, int k) {
        List<Stanica> startStations = Arrays.asList(startGrad.getAutobuskaStanica(), startGrad.getZeljeznickaStanica());
        List<Stanica> endStations = Arrays.asList(endGrad.getAutobuskaStanica(), endGrad.getZeljeznickaStanica());
        startStations = startStations.stream().filter(Objects::nonNull).collect(Collectors.toList());
        endStations = endStations.stream().filter(Objects::nonNull).collect(Collectors.toList());

        PriorityQueue<Path> pathQueue = new PriorityQueue<>((p1, p2) -> {
            return Double.compare(p1.cost, p2.cost);
        });

        List<Ruta> foundRoutes = new ArrayList<>();
        Set<String> seenPathSignatures = new HashSet<>();
        
        Map<String, Double> stationBestCost = new HashMap<>();
        Map<String, Integer> stationVisitCount = new HashMap<>();
        
        int maxIterations = 1000000;
        int maxVisitsPerStation = criterion.equals("time") ? 100 : 50;
        int iterations = 0;

        for (Stanica start : startStations) {
            Set<String> initialVisited = new HashSet<>();
            initialVisited.add(start.getId());
            // Za početnu putanju, totalMinutesAtDeparture je 0
            pathQueue.offer(new Path(start, new ArrayList<>(), 0.0, 0L, 0L, 0, initialVisited, null));
        }

        while (!pathQueue.isEmpty() && foundRoutes.size() < k && iterations < maxIterations) {
            iterations++;
            
            Path currentPath = pathQueue.poll();

            if (endStations.contains(currentPath.currentStation)) {
                if (!currentPath.segments.isEmpty()) {
                    String signature = createPathSignature(currentPath.segments);
                    if (!seenPathSignatures.contains(signature)) {
                        seenPathSignatures.add(signature);
                        Ruta ruta = new Ruta(
                            startGrad,
                            endGrad,
                            new ArrayList<>(currentPath.segments),
                            currentPath.segments.stream()
                                .filter(p -> !p.getId().startsWith("transfer_"))
                                .mapToDouble(Polazak::getCijena)
                                .sum(),
                            (int) currentPath.totalTime,
                            calculateActualTransfers(currentPath.segments)
                        );

                        if (ruta.getUkupnoVrijeme() >= 0) {
                            foundRoutes.add(ruta);
                        }
                    }
                }
                continue;
            }

            String stationKey = currentPath.currentStation.getId();
            int visitCount = stationVisitCount.getOrDefault(stationKey, 0);
            if (visitCount >= maxVisitsPerStation) {
                continue;
            }
            stationVisitCount.put(stationKey, visitCount + 1);

            String costKey = stationKey + "_" + criterion;
            Double bestCost = stationBestCost.get(costKey);
            if (bestCost != null) {
                double tolerance;
                if (criterion.equals("time")) {
                    tolerance = Math.max(120, bestCost * 0.5);
                } else if (criterion.equals("price")) {
                    tolerance = Math.max(100.0, bestCost * 0.4);
                } else {
                    tolerance = 1;
                }
                
                if (currentPath.cost > bestCost + tolerance) {
                    continue;
                }
            } else {
                stationBestCost.put(costKey, currentPath.cost);
            }

            Map<Stanica, List<Polazak>> neighbors = graph.getOrDefault(currentPath.currentStation, Collections.emptyMap());
            for (Stanica nextStation : neighbors.keySet()) {
                if (currentPath.visitedStations.contains(nextStation.getId())) {
                    continue;
                }

                for (Polazak polazak : neighbors.get(nextStation)) {
                    if (polazak.getId().startsWith("transfer_") && currentPath.segments.isEmpty()) {
                        continue;
                    }
                    
                    List<Polazak> newSegments = new ArrayList<>(currentPath.segments);
                    long nextDeparture, travelTime, waitingTime;
                    int newTransfers;

                    if (polazak.getId().startsWith("transfer_")) {
                        int minWaitTime = getTransferWaitTimeFromLastSegment(currentPath.segments);
                        
                        nextDeparture = currentPath.arrivalTime; 
                        travelTime = minWaitTime; 
                        waitingTime = 0; 
                        newTransfers = currentPath.transfers;

                        long totalMinutesSinceStartOfTransfer = currentPath.arrivalTime;
                        long totalMinutesAtEndOfTransfer = totalMinutesSinceStartOfTransfer + minWaitTime;

                        LocalTime departureTimeForDisplay = convertMinutesToLocalTime(totalMinutesSinceStartOfTransfer);
                        LocalTime arrivalTimeForDisplay = convertMinutesToLocalTime(totalMinutesAtEndOfTransfer);

                        Polazak realTransferPolazak = new Polazak(
                            polazak.getId(),
                            polazak.getPolaziste(),
                            polazak.getOdrediste(),
                            departureTimeForDisplay, 
                            arrivalTimeForDisplay,   
                            0.0,
                            minWaitTime,
                            totalMinutesSinceStartOfTransfer 
                        );
                        newSegments.add(realTransferPolazak);
                    } else {
                        nextDeparture = findNextAvailableDeparture(currentPath.arrivalTime, polazak);
                        waitingTime = nextDeparture - currentPath.arrivalTime;
                        travelTime = polazak.getTrajanje();
                        newTransfers = currentPath.transfers;
                        if (!currentPath.segments.isEmpty()) {
                            Polazak lastRegularSegment = null;
                            for (int i = currentPath.segments.size() - 1; i >= 0; i--) {
                                Polazak seg = currentPath.segments.get(i);
                                if (!seg.getId().startsWith("transfer_")) {
                                    lastRegularSegment = seg;
                                    break;
                                }
                            }
                            if (lastRegularSegment != null) {
                                boolean differentRoute = !lastRegularSegment.getId().equals(polazak.getId());
                                if (differentRoute) {
                                    newTransfers++;
                                }
                            }
                        }
                        Polazak regularPolazakWithTime = new Polazak(
                            polazak.getId(),
                            polazak.getPolaziste(),
                            polazak.getOdrediste(),
                            polazak.getVrijemePolaska(),
                            polazak.getVrijemeDolaska(),
                            polazak.getCijena(),
                            polazak.getMinimalnoVrijemeCekanja(),
                            nextDeparture 
                        );
                        newSegments.add(regularPolazakWithTime);
                    }

                    long nextArrivalTime = nextDeparture + travelTime;
                    long newTotalTime = currentPath.totalTime + waitingTime + travelTime;
                    
                    double newCost;
                    if (criterion.equals("price")) {
                        newCost = currentPath.cost + (polazak.getId().startsWith("transfer_") ? 0 : polazak.getCijena());
                    } else if (criterion.equals("transfers")) {
                        newCost = newTransfers;
                    } else {
                        newCost = newTotalTime;
                    }
                    
                    if (polazak.getId().startsWith("transfer_")) {
                        if (criterion.equals("time")) {
                            newCost += 5; 
                        } else if (criterion.equals("price")) {
                            newCost += 1.0; 
                        }
                    }

                    Set<String> newVisited = new HashSet<>(currentPath.visitedStations);
                    newVisited.add(nextStation.getId());

                    String currentTransportType = polazak.getId().startsWith("transfer_") ? 
                        "transfer" : (polazak.getPolaziste().getTip() == TipStanice.AUTOBUSKA ? "bus" : "train");

                    //Ogranicenja za velike mreze
                    if (newSegments.size() <= 100 && 
                        newTotalTime <= 1440 * 20 && 
                        newTransfers <= 30 &&
                        waitingTime >= 0) {
                        pathQueue.offer(new Path(nextStation, newSegments, newCost, 
                                               nextArrivalTime, newTotalTime, newTransfers, 
                                               newVisited, currentTransportType));
                    }
                }
            }
        }

        foundRoutes.sort((r1, r2) -> {
            switch (criterion) {
                case "time":
                    return Integer.compare(r1.getUkupnoVrijeme(), r2.getUkupnoVrijeme());
                case "price":
                    int priceCompare = Double.compare(r1.getUkupnaCijena(), r2.getUkupnaCijena());
                    if (priceCompare == 0) {
                        return Integer.compare(r1.getUkupnoVrijeme(), r2.getUkupnoVrijeme());
                    }
                    return priceCompare;
                case "transfers":
                    int transferCompare = Integer.compare(calculateActualTransfers(r1.getPolasci()), 
                                                        calculateActualTransfers(r2.getPolasci()));
                    if (transferCompare == 0) {
                        return Integer.compare(r1.getUkupnoVrijeme(), r2.getUkupnoVrijeme());
                    }
                    return transferCompare;
                default:
                    return Integer.compare(r1.getUkupnoVrijeme(), r2.getUkupnoVrijeme());
            }
        });

        return foundRoutes.stream().limit(k).collect(Collectors.toList());
    }

    
    /**
     * Calculates the actual number of transfers in a list of departure segments, ignoring
     * transfers within the same route.
     *
     * @param segments The list of departure segments.
     * @return The number of actual transfers.
     */
    private int calculateActualTransfers(List<Polazak> segments) {
        if (segments.size() <= 1) return 0;
        
        int transfers = 0;
        Polazak previousRegular = null;
        
        for (Polazak segment : segments) {
            if (segment.getId().startsWith("transfer_")) {
                continue;
            }
            
            if (previousRegular != null) {
                boolean differentRoute = !previousRegular.getId().equals(segment.getId());
                if (differentRoute) {
                    transfers++;
                }
            }
            
            previousRegular = segment;
        }
        
        return transfers;
    }
    
    
    /**
     * Converts a total number of minutes since the start into a LocalTime object,
     * representing the time of day.
     *
     * @param totalMinutes The total minutes since the start.
     * @return The corresponding LocalTime object.
     */
    private LocalTime convertMinutesToLocalTime(long totalMinutes) {
        long minutesInDay = totalMinutes % 1440; 
        int hour = (int) (minutesInDay / 60);
        int minute = (int) (minutesInDay % 60);
        return LocalTime.of(hour, minute);
    }
    
    /**
     * Inner class representing a path in the search process, including the current station,
     * segments, cost, and other relevant metrics.
     */
    private static class Path {
        Stanica currentStation;
        List<Polazak> segments;
        double cost;
        long arrivalTime;
        long totalTime;
        int transfers;
        Set<String> visitedStations;

        
        /**
         * Constructs a Path object with the given parameters.
         *
         * @param station The current station in the path.
         * @param segments The list of departure segments in the path.
         * @param cost The cost of the path based on the criterion.
         * @param arrivalTime The arrival time at the current station in minutes.
         * @param totalTime The total travel time in minutes.
         * @param transfers The number of transfers in the path.
         * @param visitedStations The set of visited station IDs.
         * @param lastTransportType The type of the last transport used.
         */
        Path(Stanica station, List<Polazak> segments, double cost, long arrivalTime, 
             long totalTime, int transfers, Set<String> visitedStations, String lastTransportType) {
            this.currentStation = station;
            this.segments = segments;
            this.cost = cost;
            this.arrivalTime = arrivalTime;
            this.totalTime = totalTime;
            this.transfers = transfers;
            this.visitedStations = visitedStations;
        }
    }
}
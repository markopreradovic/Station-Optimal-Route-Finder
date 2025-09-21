# Optimal Route Finder - Java GUI Application

## Overview
A **Java GUI application** for finding optimal travel routes between cities within a country.  
Users can combine **bus** and **train transportation** while optimizing by:
- **Shortest travel time**
- **Lowest ticket price**
- **Fewest transfers**

The country is represented as an **n × m grid of cities**, each with a bus and train station. Travel data (departures, arrivals, prices, waiting times) is loaded from JSON.

## Key Features
- Select **origin** and **destination** city.
- Choose optimization criteria.
- Display the **optimal route** with details.
- Show **Top 5 alternative routes**.
- **Graph visualization** (JavaFX Canvas / GraphStream).
- **Ticket purchase simulation**:
  - Generates receipts (`/receipts` folder).
  - Tracks total tickets sold & revenue across sessions.

## Technologies
- **JavaFX ** for GUI  
- **GraphStream** for graph visualization  
- **Gson (GsonBuilder)** for JSON parsing  
- **Custom KShortestPathsFinder** for route calculation  

## Project Structure
- **Data model**: Cities, stations, departures  
- **Algorithms**: Dijkstra & K-Shortest Paths  
- **Persistence**: JSON input + receipts output  
- **GUI**: Interactive map, forms, tables, graph view  

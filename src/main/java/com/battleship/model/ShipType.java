package com.battleship.model;

public enum ShipType {
    CARRIER(5, "Carrier / Porte-avions"),
    BATTLESHIP(4, "Battleship / Cuirassé"),
    CRUISER(3, "Cruiser / Croiseur"),
    SUBMARINE(3, "Submarine / Sous-marin"),
    DESTROYER(2, "Destroyer / Contre-torpilleur");

    private final int size;
    private final String displayName;

    ShipType(int size, String displayName) {
        this.size = size;
        this.displayName = displayName;
    }

    public int getSize() {
        return size;
    }

    public String getDisplayName() {
        return displayName;
    }
}

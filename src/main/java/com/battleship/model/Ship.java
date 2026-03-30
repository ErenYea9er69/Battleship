package com.battleship.model;

import java.util.ArrayList;
import java.util.List;

public class Ship {
    private final ShipType type;
    private final List<Position> positions;
    private int hits;

    public Ship(ShipType type, List<Position> positions) {
        this.type = type;
        this.positions = new ArrayList<>(positions);
        this.hits = 0;
    }

    public ShipType getType() {
        return type;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void hit() {
        hits++;
    }

    public boolean isSunk() {
        return hits >= type.getSize();
    }
}

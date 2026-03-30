package com.battleship.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final int SIZE = 10;
    private final CellState[][] grid;
    private final List<Ship> ships;
    private int shipsSunk;

    public Board() {
        grid = new CellState[SIZE][SIZE];
        ships = new ArrayList<>();
        shipsSunk = 0;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = CellState.WATER;
            }
        }
    }

    public boolean canPlaceShip(ShipType type, int startX, int startY, boolean horizontal) {
        if (horizontal) {
            if (startX + type.getSize() > SIZE) return false;
            for (int i = 0; i < type.getSize(); i++) {
                if (grid[startX + i][startY] != CellState.WATER) return false;
            }
        } else {
            if (startY + type.getSize() > SIZE) return false;
            for (int j = 0; j < type.getSize(); j++) {
                if (grid[startX][startY + j] != CellState.WATER) return false;
            }
        }
        return true;
    }

    public boolean placeShip(ShipType type, int startX, int startY, boolean horizontal) {
        if (!canPlaceShip(type, startX, startY, horizontal)) return false;

        List<Position> posList = new ArrayList<>();
        if (horizontal) {
            for (int i = 0; i < type.getSize(); i++) {
                grid[startX + i][startY] = CellState.SHIP;
                posList.add(new Position(startX + i, startY));
            }
        } else {
            for (int j = 0; j < type.getSize(); j++) {
                grid[startX][startY + j] = CellState.SHIP;
                posList.add(new Position(startX, startY + j));
            }
        }
        ships.add(new Ship(type, posList));
        return true;
    }

    public void placeAllShipsRandomly() {
        ships.clear();
        shipsSunk = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = CellState.WATER;
            }
        }

        for (ShipType type : ShipType.values()) {
            boolean placed = false;
            while (!placed) {
                int startX = (int) (Math.random() * SIZE);
                int startY = (int) (Math.random() * SIZE);
                boolean horizontal = Math.random() < 0.5;
                placed = placeShip(type, startX, startY, horizontal);
            }
        }
    }

    public ShotResult shoot(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return new ShotResult(false, null, false);
        
        CellState state = grid[x][y];
        if (state == CellState.HIT || state == CellState.MISS || state == CellState.SUNK) {
            return new ShotResult(false, null, false); // Already shot here
        }

        if (state == CellState.WATER) {
            grid[x][y] = CellState.MISS;
            return new ShotResult(true, CellState.MISS, false);
        }

        if (state == CellState.SHIP) {
            grid[x][y] = CellState.HIT;
            // Find which ship was hit
            for (Ship ship : ships) {
                for (Position pos : ship.getPositions()) {
                    if (pos.x() == x && pos.y() == y) {
                        ship.hit();
                        boolean newlySunk = ship.isSunk();
                        if (newlySunk) {
                            shipsSunk++;
                            // Mark all positions of the sunk ship
                            for (Position shipPos : ship.getPositions()) {
                                grid[shipPos.x()][shipPos.y()] = CellState.SUNK;
                            }
                        }
                        return new ShotResult(true, CellState.HIT, newlySunk, newlySunk ? ship : null);
                    }
                }
            }
        }
        return new ShotResult(false, null, false);
    }

    public boolean areAllShipsSunk() {
        return shipsSunk == ships.size() && !ships.isEmpty();
    }
    
    public int getShipsRemaining() {
        return ships.size() - shipsSunk;
    }

    public CellState getCellState(int x, int y) {
        return grid[x][y];
    }
    
    public List<Ship> getShips() {
        return ships;
    }

    public record ShotResult(boolean valid, CellState resultState, boolean sunk, Ship sunkShip) {
        public ShotResult(boolean valid, CellState resultState, boolean sunk) {
            this(valid, resultState, sunk, null);
        }
    }
}

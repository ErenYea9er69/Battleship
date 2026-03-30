package com.battleship.ai;

import com.battleship.model.Board;
import com.battleship.model.CellState;
import com.battleship.model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EnemyAI {
    private final Board targetBoard;
    private final Random random = new Random();
    
    private final List<Position> potentialTargets = new ArrayList<>();
    private Position firstHit = null;

    public EnemyAI(Board targetBoard) {
        this.targetBoard = targetBoard;
    }

    public Position determineNextShot() {
        while (!potentialTargets.isEmpty()) {
            Position p = potentialTargets.remove(0); // DFS/BFS approach
            if (isValidTarget(p.x(), p.y())) {
                return p;
            }
        }
        
        // Random hunting
        firstHit = null;
        while (true) {
            int x = random.nextInt(Board.SIZE);
            int y = random.nextInt(Board.SIZE);
            if (isValidTarget(x, y)) {
                return new Position(x, y);
            }
        }
    }

    public void recordResult(Position pos, Board.ShotResult result) {
        if (result.resultState() == CellState.HIT && !result.sunk()) {
            if (firstHit == null) {
                firstHit = pos;
            }
            addAdjacentTargets(pos);
        } else if (result.sunk()) {
            // Ship sunk, clear targets to hunt again
            firstHit = null;
            potentialTargets.clear(); 
        }
    }

    private boolean isValidTarget(int x, int y) {
        if (x < 0 || x >= Board.SIZE || y < 0 || y >= Board.SIZE) return false;
        CellState state = targetBoard.getCellState(x, y);
        // AI can only shoot at unknown tiles or revealed player ship that isn't hit
        return state == CellState.WATER || state == CellState.SHIP;
    }

    private void addAdjacentTargets(Position pos) {
        List<Position> newTargets = new ArrayList<>();
        if (isValidTarget(pos.x() + 1, pos.y())) newTargets.add(new Position(pos.x() + 1, pos.y()));
        if (isValidTarget(pos.x() - 1, pos.y())) newTargets.add(new Position(pos.x() - 1, pos.y()));
        if (isValidTarget(pos.x(), pos.y() + 1)) newTargets.add(new Position(pos.x(), pos.y() + 1));
        if (isValidTarget(pos.x(), pos.y() - 1)) newTargets.add(new Position(pos.x(), pos.y() - 1));
        
        Collections.shuffle(newTargets);
        potentialTargets.addAll(0, newTargets); // Target adjacent quickly
    }
}

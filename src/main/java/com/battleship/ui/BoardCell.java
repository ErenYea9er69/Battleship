package com.battleship.ui;

import com.battleship.model.Position;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class BoardCell extends StackPane {
    private final Position position;
    private final boolean isPlayerBoard;
    private final Label marker;

    public BoardCell(int x, int y, boolean isPlayerBoard) {
        this.position = new Position(x, y);
        this.isPlayerBoard = isPlayerBoard;
        
        this.marker = new Label();
        this.marker.getStyleClass().add("cell-marker");
        
        getChildren().add(marker);
        reset();
    }

    public Position getPosition() {
        return position;
    }
    
    public boolean isPlayerBoard() {
        return isPlayerBoard;
    }
    
    public void setHit() {
        getStyleClass().removeAll("water", "player-ship", "hoverable");
        if (!getStyleClass().contains("hit")) getStyleClass().add("hit");
        marker.setText("X");
    }
    
    public void setMiss() {
        getStyleClass().removeAll("water", "player-ship", "hoverable");
        if (!getStyleClass().contains("miss")) getStyleClass().add("miss");
        marker.setText("•");
    }
    
    public void setSunk() {
        getStyleClass().removeAll("hit");
        if (!getStyleClass().contains("sunk")) getStyleClass().add("sunk");
    }
    
    public void setShip() {
        getStyleClass().remove("water");
        if (!getStyleClass().contains("player-ship")) getStyleClass().add("player-ship");
    }
    
    public void setHoverable(boolean hoverable) {
        if (hoverable) {
            if (!getStyleClass().contains("hoverable")) getStyleClass().add("hoverable");
        } else {
            getStyleClass().remove("hoverable");
        }
    }
    
    // For styling the placement shadow during setup
    public void setPlacementShadow(boolean valid) {
        getStyleClass().remove("shadow-valid");
        getStyleClass().remove("shadow-invalid");
        if (valid) {
            getStyleClass().add("shadow-valid");
        } else {
            getStyleClass().add("shadow-invalid");
        }
    }
    
    public void clearShadow() {
        getStyleClass().remove("shadow-valid");
        getStyleClass().remove("shadow-invalid");
    }
    
    public void reset() {
        marker.setText("");
        getStyleClass().clear();
        getStyleClass().add("cell");
        getStyleClass().add("water");
    }
}

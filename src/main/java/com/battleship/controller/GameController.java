package com.battleship.controller;

import com.battleship.ai.EnemyAI;
import com.battleship.model.Board;
import com.battleship.model.CellState;
import com.battleship.model.GameState;
import com.battleship.model.Position;
import com.battleship.model.ShipType;
import com.battleship.ui.BoardCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

public class GameController {

    @FXML private GridPane playerGrid;
    @FXML private GridPane enemyGrid;
    @FXML private Label statusLabel;
    @FXML private Label playerShipsLabel;
    @FXML private Label enemyShipsLabel;
    @FXML private ListView<ShipType> shipListView;
    @FXML private Button toggleOrientationBtn;
    @FXML private Button autoPlaceBtn;
    @FXML private Button startGameBtn;
    @FXML private Button newGameBtn;

    private Board playerBoard;
    private Board enemyBoard;
    private EnemyAI ai;
    private GameState currentState;
    private boolean isHorizontalPlacement = true;
    
    private BoardCell[][] playerCells;
    private BoardCell[][] enemyCells;
    private ObservableList<ShipType> unplacedShips;

    @FXML
    public void initialize() {
        setupShipList();
        startNewGame();
    }

    private void setupShipList() {
        shipListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ShipType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName() + " [" + item.getSize() + "]");
                }
            }
        });
    }

    @FXML
    private void handleToggleOrientation() {
        isHorizontalPlacement = !isHorizontalPlacement;
        toggleOrientationBtn.setText(isHorizontalPlacement ? 
            "Toggle Orientation: Horizontal" : "Toggle Orientation: Vertical");
    }

    @FXML
    private void handleAutoPlace() {
        if (currentState != GameState.SETUP) return;
        
        playerBoard.placeAllShipsRandomly();
        unplacedShips.clear();
        updatePlayerGridVisuals();
        checkSetupComplete();
    }

    @FXML
    private void handleStartGame() {
        if (currentState != GameState.SETUP || !unplacedShips.isEmpty()) return;
        
        enemyBoard.placeAllShipsRandomly();
        currentState = GameState.PLAYER_TURN;
        
        shipListView.setDisable(true);
        toggleOrientationBtn.setDisable(true);
        autoPlaceBtn.setDisable(true);
        startGameBtn.setDisable(true);
        
        updateEnemyGridVisuals();
        setStatus("Game Started! Your turn to shoot. / Jeu démarré ! À vous de tirer.");
        updateHoverStates();
    }

    @FXML
    private void handleNewGame() {
        startNewGame();
    }

    private void startNewGame() {
        playerBoard = new Board();
        enemyBoard = new Board();
        ai = new EnemyAI(playerBoard);
        currentState = GameState.SETUP;
        
        playerGrid.getChildren().clear();
        enemyGrid.getChildren().clear();
        
        playerCells = new BoardCell[Board.SIZE][Board.SIZE];
        enemyCells = new BoardCell[Board.SIZE][Board.SIZE];
        
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                BoardCell pCell = new BoardCell(i, j, true);
                setupPlayerCellInteractions(pCell);
                playerGrid.add(pCell, i, j);
                playerCells[i][j] = pCell;

                BoardCell eCell = new BoardCell(i, j, false);
                setupEnemyCellInteractions(eCell);
                enemyGrid.add(eCell, i, j);
                enemyCells[i][j] = eCell;
            }
        }
        
        unplacedShips = FXCollections.observableArrayList(ShipType.values());
        shipListView.setItems(unplacedShips);
        if (!unplacedShips.isEmpty()) {
            shipListView.getSelectionModel().selectFirst();
        }
        
        shipListView.setDisable(false);
        toggleOrientationBtn.setDisable(false);
        autoPlaceBtn.setDisable(false);
        startGameBtn.setDisable(true);
        
        updateScoreLabels();
        setStatus("Place your ships / Placez vos navires!");
        updateHoverStates();
    }

    private void setupPlayerCellInteractions(BoardCell cell) {
        cell.setOnMouseEntered(e -> {
            if (currentState != GameState.SETUP || unplacedShips.isEmpty()) return;
            ShipType selected = shipListView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            
            boolean valid = playerBoard.canPlaceShip(selected, cell.getPosition().x(), cell.getPosition().y(), isHorizontalPlacement);
            
            drawPlacementShadow(selected, cell.getPosition().x(), cell.getPosition().y(), isHorizontalPlacement, valid);
        });

        cell.setOnMouseExited(e -> {
            if (currentState != GameState.SETUP) return;
            clearAllShadows();
        });

        cell.setOnMouseClicked(e -> {
            if (currentState != GameState.SETUP || unplacedShips.isEmpty()) return;
            ShipType selected = shipListView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            
            int x = cell.getPosition().x();
            int y = cell.getPosition().y();
            
            if (playerBoard.placeShip(selected, x, y, isHorizontalPlacement)) {
                unplacedShips.remove(selected);
                clearAllShadows();
                updatePlayerGridVisuals();
                checkSetupComplete();
            } else {
                setStatus("Cannot place ship there! / Impossible de placer le navire ici !");
            }
        });
    }

    private void setupEnemyCellInteractions(BoardCell cell) {
        cell.setOnMouseClicked(e -> {
            if (currentState != GameState.PLAYER_TURN) return;
            
            int x = cell.getPosition().x();
            int y = cell.getPosition().y();
            
            Board.ShotResult result = enemyBoard.shoot(x, y);
            if (result.valid()) {
                updateEnemyGridVisuals();
                updateScoreLabels();
                
                if (result.sunk()) {
                    setStatus("You sank the " + result.sunkShip().getType().getDisplayName() + "!");
                } else if (result.resultState() == CellState.HIT) {
                    setStatus("Hit! / Touché !");
                } else {
                    setStatus("Miss! / À l'eau !");
                }

                if (enemyBoard.areAllShipsSunk()) {
                    endGame(true);
                } else {
                    currentState = GameState.AI_TURN;
                    updateHoverStates();
                    // Small delay for AI turn
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(500); 
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        processAITurn();
                    });
                }
            }
        });
    }

    private void processAITurn() {
        if (currentState != GameState.AI_TURN) return;
        
        Position target = ai.determineNextShot();
        Board.ShotResult result = playerBoard.shoot(target.x(), target.y());
        ai.recordResult(target, result);
        
        updatePlayerGridVisuals();
        updateScoreLabels();
        
        if (result.sunk()) {
            setStatus("Enemy sank your " + result.sunkShip().getType().getDisplayName() + "!");
        } else if (result.resultState() == CellState.HIT) {
            setStatus("Enemy hit your ship! / L'ennemi a touché votre navire !");
        } else {
            setStatus("Enemy missed! / L'ennemi a manqué !");
        }

        if (playerBoard.areAllShipsSunk()) {
            endGame(false);
        } else {
            currentState = GameState.PLAYER_TURN;
            updateHoverStates();
        }
    }

    private void drawPlacementShadow(ShipType type, int startX, int startY, boolean horizontal, boolean valid) {
        clearAllShadows();
        int size = type.getSize();
        if (horizontal) {
            for (int i = 0; i < size; i++) {
                if (startX + i < Board.SIZE) {
                    playerCells[startX + i][startY].setPlacementShadow(valid);
                }
            }
        } else {
            for (int j = 0; j < size; j++) {
                if (startY + j < Board.SIZE) {
                    playerCells[startX][startY + j].setPlacementShadow(valid);
                }
            }
        }
    }

    private void clearAllShadows() {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                playerCells[i][j].clearShadow();
            }
        }
    }

    private void updatePlayerGridVisuals() {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                CellState state = playerBoard.getCellState(i, j);
                updateCellVisual(playerCells[i][j], state, true);
            }
        }
    }

    private void updateEnemyGridVisuals() {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                CellState state = enemyBoard.getCellState(i, j);
                updateCellVisual(enemyCells[i][j], state, false);
            }
        }
    }

    private void updateCellVisual(BoardCell cell, CellState state, boolean isPlayer) {
        switch (state) {
            case WATER -> {
                cell.reset();
            }
            case SHIP -> {
                if (isPlayer) cell.setShip();
                else cell.reset(); // Hide enemy ships
            }
            case HIT -> cell.setHit();
            case MISS -> cell.setMiss();
            case SUNK -> cell.setSunk();
        }
    }

    private void updateHoverStates() {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                boolean canPlayerHover = currentState == GameState.SETUP && !unplacedShips.isEmpty();
                playerCells[i][j].setHoverable(canPlayerHover);
                
                boolean canEnemyHover = currentState == GameState.PLAYER_TURN && 
                        (enemyBoard.getCellState(i, j) == CellState.WATER || 
                         enemyBoard.getCellState(i, j) == CellState.SHIP);
                enemyCells[i][j].setHoverable(canEnemyHover);
            }
        }
    }

    private void updateScoreLabels() {
        playerShipsLabel.setText(String.format("Player Ships Remaining: %d", playerBoard.getShipsRemaining()));
        enemyShipsLabel.setText(String.format("Enemy Ships Remaining: %d", enemyBoard.getShipsRemaining()));
    }

    private void checkSetupComplete() {
        if (unplacedShips.isEmpty()) {
            startGameBtn.setDisable(false);
            setStatus("All ships placed. Ready to start! / Tous les navires sont placés.");
            if (!shipListView.getItems().isEmpty()) {
                shipListView.getSelectionModel().clearSelection();
            }
        } else {
            shipListView.getSelectionModel().selectFirst();
        }
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    private void endGame(boolean playerWon) {
        currentState = GameState.GAME_OVER;
        updateHoverStates();
        // Reveal remaining enemy ships if player lost
        if (!playerWon) {
            for (int i = 0; i < Board.SIZE; i++) {
                for (int j = 0; j < Board.SIZE; j++) {
                    if (enemyBoard.getCellState(i, j) == CellState.SHIP) {
                        enemyCells[i][j].setShip();
                    }
                }
            }
        }
        
        String title = playerWon ? "Victory! / Victoire !" : "Defeat! / Défaite !";
        String message = playerWon ? 
            "Congratulations, you destroyed the enemy fleet!" : 
            "All your ships have been sunk!";
            
        setStatus(title + " " + message);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        ButtonType playAgainBtn = new ButtonType("Play Again");
        ButtonType exitBtn = new ButtonType("Exit");
        alert.getButtonTypes().setAll(playAgainBtn, exitBtn);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == playAgainBtn) {
            startNewGame();
        } else if (result.isPresent() && result.get() == exitBtn) {
            Platform.exit();
        }
    }
}

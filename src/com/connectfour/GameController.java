package com.connectfour;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    private static final int TILE_SIZE = 80;
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    
    // UI components
    @FXML private Label statusLabel;
    @FXML private StackPane gameArea;
    @FXML private Pane discRoot;
    
    private Board gameBoard;
    // To track active discs so we can clear them on reset
    private List<Circle> placedDiscs;
    // Prevent multiple animations from breaking state
    private boolean isAnimating = false;

    public GameController() {
        gameBoard = new Board();
        placedDiscs = new ArrayList<>();
    }

    @FXML
    public void initialize() {
        // Initialization if required
    }
    
    @FXML
    public void handleColumnHover(javafx.scene.input.MouseEvent event) {
        Pane source = (Pane) event.getSource();
        if (!gameBoard.isGameOver() && !isAnimating) {
            source.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255, 255, 255, 0.1);");
        }
    }

    @FXML
    public void handleColumnExit(javafx.scene.input.MouseEvent event) {
        Pane source = (Pane) event.getSource();
        source.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
    }

    @FXML
    public void handleColumnClick(javafx.scene.input.MouseEvent event) {
        if (gameBoard.isGameOver() || isAnimating) return;
        Pane source = (Pane) event.getSource();
        int col = Integer.parseInt(source.getUserData().toString());
        
        int row = gameBoard.dropDisc(col);
        if (row != -1) {
            isAnimating = true;
            // Reset hover
            source.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
            placeDiscVisual(row, col);
        }
    }



    private void placeDiscVisual(int row, int col) {
        Circle disc = new Circle(TILE_SIZE / 2.5);
        disc.setCenterX(col * TILE_SIZE + TILE_SIZE / 2.0);
        // Start above the board
        disc.setCenterY(-TILE_SIZE / 2.0); 

        // Player colors
        int player = gameBoard.getCurrentPlayer();
        if (player == 1) {
            disc.setFill(Color.web("#e74c3c")); // Red
        } else {
            disc.setFill(Color.web("#f1c40f")); // Yellow
        }
        
        discRoot.getChildren().add(disc);
        placedDiscs.add(disc);

        // End position
        double endY = row * TILE_SIZE + TILE_SIZE / 2.0;
        double travelDistance = endY - disc.getCenterY();

        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.4), disc);
        transition.setToY(travelDistance);
        
        transition.setOnFinished(e -> {
            boolean win = gameBoard.checkWin(row, col);
            if (win) {
                gameBoard.setGameOver(true);
                showGameEnd("Player " + player + " Wins!");
            } else if (gameBoard.isBoardFull()) {
                gameBoard.setGameOver(true);
                showGameEnd("It's a Draw!");
            } else {
                gameBoard.switchPlayer();
                updateTurnLabel();
                isAnimating = false;
            }
        });

        transition.play();
    }

    private void updateTurnLabel() {
        if (gameBoard.getCurrentPlayer() == 1) {
            statusLabel.setText("Player 1's Turn (Red)");
            statusLabel.setTextFill(Color.web("#e74c3c"));
        } else {
            statusLabel.setText("Player 2's Turn (Yellow)");
            statusLabel.setTextFill(Color.web("#f1c40f"));
        }
    }

    private void showGameEnd(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.WHITE);

        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText(message + "\nDo you want to play again?");
            
            ButtonType playAgain = new ButtonType("Play Again");
            ButtonType quit = new ButtonType("Quit");
            alert.getButtonTypes().setAll(playAgain, quit);

            alert.showAndWait().ifPresent(res -> {
                if (res == playAgain) {
                    resetGame();
                } else {
                    System.exit(0);
                }
            });
            isAnimating = false;
        });
    }

    private void resetGame() {
        gameBoard.reset();
        discRoot.getChildren().removeAll(placedDiscs);
        placedDiscs.clear();
        updateTurnLabel();
    }
}

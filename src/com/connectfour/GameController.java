package com.connectfour;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
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
    private BorderPane root;
    private Pane discRoot;
    private Label statusLabel;
    
    private Board gameBoard;
    // To track active discs so we can clear them on reset
    private List<Circle> placedDiscs;
    // Prevent multiple animations from breaking state
    private boolean isAnimating = false;

    public GameController() {
        gameBoard = new Board();
        placedDiscs = new ArrayList<>();
        createUI();
    }

    private void createUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        // Top Header
        statusLabel = new Label("Player 1's Turn (Red)");
        statusLabel.setTextFill(Color.web("#e74c3c"));
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        HBox topBox = new HBox(statusLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setStyle("-fx-padding: 20px;");
        root.setTop(topBox);

        // Game Area
        StackPane gameArea = new StackPane();
        gameArea.setStyle("-fx-background-color: transparent;");

        discRoot = new Pane();
        discRoot.setPrefSize(COLUMNS * TILE_SIZE, ROWS * TILE_SIZE);
        
        Shape boardOverlay = createBoardOverlay();
        boardOverlay.setFill(Color.web("#2980b9"));
        // Add subtle shadow or styling
        boardOverlay.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        Pane clickHandlers = createClickHandlers();

        gameArea.getChildren().addAll(discRoot, boardOverlay, clickHandlers);
        // Force dimensions
        gameArea.setMaxSize(COLUMNS * TILE_SIZE, ROWS * TILE_SIZE);

        root.setCenter(gameArea);
        BorderPane.setAlignment(gameArea, Pos.CENTER);
    }

    private Shape createBoardOverlay() {
        Shape boardShape = new Rectangle(COLUMNS * TILE_SIZE, ROWS * TILE_SIZE);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                // Determine center of each circle hole
                double cx = c * TILE_SIZE + TILE_SIZE / 2.0;
                double cy = r * TILE_SIZE + TILE_SIZE / 2.0;
                Circle circleHole = new Circle(cx, cy, TILE_SIZE / 2.5); // Slightly smaller than half tile
                boardShape = Shape.subtract(boardShape, circleHole);
            }
        }
        return boardShape;
    }

    private Pane createClickHandlers() {
        Pane clickPane = new Pane();
        for (int c = 0; c < COLUMNS; c++) {
            Rectangle rect = new Rectangle(TILE_SIZE, ROWS * TILE_SIZE);
            rect.setX(c * TILE_SIZE);
            rect.setFill(Color.TRANSPARENT);

            // Hover effects
            rect.setOnMouseEntered(e -> {
                if (!gameBoard.isGameOver() && !isAnimating) {
                    rect.setFill(Color.rgb(255, 255, 255, 0.1));
                }
            });
            rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));

            // Drop logic
            final int col = c;
            rect.setOnMouseClicked(e -> {
                if (gameBoard.isGameOver() || isAnimating) return;
                
                int row = gameBoard.dropDisc(col);
                if (row != -1) {
                    isAnimating = true;
                    // Reset hover
                    rect.setFill(Color.TRANSPARENT);
                    placeDiscVisual(row, col);
                }
            });

            clickPane.getChildren().add(rect);
        }
        return clickPane;
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
    }

    private void resetGame() {
        gameBoard.reset();
        discRoot.getChildren().removeAll(placedDiscs);
        placedDiscs.clear();
        updateTurnLabel();
    }

    public BorderPane getRootPane() {
        return root;
    }
}

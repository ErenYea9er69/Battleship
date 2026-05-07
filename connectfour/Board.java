package com.connectfour;

public class Board {
    public static final int ROWS = 6;
    public static final int COLUMNS = 7;
    public static final int EMPTY = 0;

    private int[][] grid;
    private int currentPlayer;
    private boolean gameOver;

    public Board() {
        grid = new int[ROWS][COLUMNS];
        currentPlayer = 1;
        gameOver = false;
        reset();
    }

    public void reset() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                grid[row][col] = EMPTY;
            }
        }
        currentPlayer = 1; 
        gameOver = false;
    }

    public int dropDisc(int col) {
        if (gameOver || col < 0 || col >= COLUMNS) {
            return -1;
        }

        for (int row = ROWS - 1; row >= 0; row--) {
            if (grid[row][col] == EMPTY) {
                grid[row][col] = currentPlayer;
                return row;
            }
        }
        return -1; 
    }

 
    public boolean checkWin(int checkRow, int checkCol) {
        int player = grid[checkRow][checkCol];
        if (player == EMPTY) return false;

        return checkDirection(checkRow, checkCol, player, 0, 1) ||
               checkDirection(checkRow, checkCol, player, 1, 0) ||  
               checkDirection(checkRow, checkCol, player, 1, 1) || 
               checkDirection(checkRow, checkCol, player, 1, -1);   
    }

    private boolean checkDirection(int row, int col, int player, int deltaRow, int deltaCol) {
        int count = 1;

        int r = row + deltaRow;
        int c = col + deltaCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLUMNS && grid[r][c] == player) {
            count++;
            r += deltaRow;
            c += deltaCol;
        }

        r = row - deltaRow;
        c = col - deltaCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLUMNS && grid[r][c] == player) {
            count++;
            r -= deltaRow;
            c -= deltaCol;
        }

        return count >= 4;
    }

    public boolean isBoardFull() {
        for (int col = 0; col < COLUMNS; col++) {
            if (grid[0][col] == EMPTY) {
                return false;
            }
        }
        return true;
    }

    public void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCell(int row, int col) {
        return grid[row][col];
    }
    
    public void setGameOver(boolean b) {
        this.gameOver = b;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}

package com.poker.score.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TableGame {
    private int gameId;
    private int tableId;
    private int gameSequence;
    private int bidAmount;
    @JsonProperty
    private boolean isRunning;

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(int bidAmount) {
        this.bidAmount = bidAmount;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public int getGameSequence() {
        return gameSequence;
    }

    public void setGameSequence(int gameSequence) {
        this.gameSequence = gameSequence;
    }
}

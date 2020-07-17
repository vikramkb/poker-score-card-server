package com.poker.score.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class TableGame {
    private int gameId;
    private int tableId;
    private int gameSequence;
    private Timestamp timestamp;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

package com.poker.score.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TableUpdateInfo {
    private int tableId;
    private int gameId;
    @JsonProperty
    private boolean isRunning;

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}

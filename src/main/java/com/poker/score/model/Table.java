package com.poker.score.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Table {
    private int tableId;
    private String tableName;
    private String createdPlayerName;
    @JsonProperty
    private boolean isRunning;

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCreatedPlayerName() {
        return createdPlayerName;
    }

    public void setCreatedPlayerName(String createdPlayerName) {
        this.createdPlayerName = createdPlayerName;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}

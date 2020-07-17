package com.poker.score.model;

import java.util.List;

public class TableGamePlayerScore {
    private int gameSequence;
    private int tableId;
    private List<String> playerNames;
    private List<Integer> scores;

    public int getGameSequence() {
        return gameSequence;
    }

    public void setGameSequence(int gameSequence) {
        this.gameSequence = gameSequence;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }

    public List<Integer> getScores() {
        return scores;
    }

    public void setScores(List<Integer> scores) {
        this.scores = scores;
    }
}

package com.poker.score.model;

import java.sql.Timestamp;
import java.util.List;

public class TableGameRoundPlayer {
    private int gameId;
    private int tableId;
    private int roundId;
    private int bidAmount;
    private int roundSequence;
    private List<String> playerNames;
    private Timestamp timestamp;

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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }

    public int getRoundSequence() {
        return roundSequence;
    }

    public void setRoundSequence(int roundSequence) {
        this.roundSequence = roundSequence;
    }

    public int getRoundId() {
        return roundId;
    }

    public void setRoundId(int roundId) {
        this.roundId = roundId;
    }
}

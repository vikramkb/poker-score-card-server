package com.poker.score.model;

import java.util.List;

public class FullTable {
    private Table table;
    private TablePlayer players;
    private List<TableGame> games;
    private List<TableGamePlayerScore> gamesScore;
    private TablePlayerTotalScore tableTotalScore;
    private List<TableGameRoundPlayer> tableGameRoundPlayers;


    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public TablePlayer getPlayers() {
        return players;
    }

    public void setPlayers(TablePlayer players) {
        this.players = players;
    }

    public List<TableGame> getGames() {
        return games;
    }

    public void setGames(List<TableGame> games) {
        this.games = games;
    }

    public List<TableGamePlayerScore> getGamesScore() {
        return gamesScore;
    }

    public void setGamesScore(List<TableGamePlayerScore> gamesScore) {
        this.gamesScore = gamesScore;
    }

    public TablePlayerTotalScore getTableTotalScore() {
        return tableTotalScore;
    }

    public void setTableTotalScore(TablePlayerTotalScore tableTotalScore) {
        this.tableTotalScore = tableTotalScore;
    }

    public List<TableGameRoundPlayer> getTableGameRoundPlayers() {
        return tableGameRoundPlayers;
    }

    public void setTableGameRoundPlayers(List<TableGameRoundPlayer> tableGameRoundPlayers) {
        this.tableGameRoundPlayers = tableGameRoundPlayers;
    }
}

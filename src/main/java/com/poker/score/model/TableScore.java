package com.poker.score.model;

public class TableScore {
    private Table table;
    private TablePlayerTotalScore tablePlayerTotalScore;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public TablePlayerTotalScore getTablePlayerTotalScore() {
        return tablePlayerTotalScore;
    }

    public void setTablePlayerTotalScore(TablePlayerTotalScore tablePlayerTotalScore) {
        this.tablePlayerTotalScore = tablePlayerTotalScore;
    }
}

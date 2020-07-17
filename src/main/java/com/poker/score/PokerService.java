package com.poker.score;

import com.poker.score.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
public class PokerService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    void createTable(Table table) {
        if(table.getTableId() < 0) {
            throw new IllegalArgumentException("invalid table id");
        }
        int playerId = getPlayerId(table.getCreatedPlayerName());
        jdbcTemplate.update("insert into poker_table(currnt_timestamp, table_name, created_player_id, is_running) values(CURRENT_TIMESTAMP, ?, ?, ?)", table.getTableName(), playerId, table.isRunning() ? 1 : 0);
    }

    private int getPlayerId(String playerName) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("player_name", playerName);
        List<Integer> playerIds = namedParameterJdbcTemplate.query(
            "select player_id from player where player_name = :player_name", parameters,
            (rs, rowNum) -> rs.getInt("player_id"));

        if(playerIds.size() != 1) {
            throw new IllegalArgumentException("these players are not on the table");
        }
        return playerIds.get(0);
    }

    private List<Integer>  getPlayerIds(List<String> playerNames) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("player_names", playerNames);
        List<Integer> playerIds = namedParameterJdbcTemplate.query(
            "select player_id from player where player_name in (:player_names)", parameters,
            (rs, rowNum) -> rs.getInt("player_id"));

        return playerIds;
    }

    public void createPlayer(Player player) {
        Pattern pattern = Pattern.compile("^\\d{10}$");
        if(isBlank(player.getPlayerName()) || isBlank(player.getFullName()) || !pattern.matcher(player.getPhone()).matches()) {
            throw new IllegalArgumentException("invalid phone number");
        }
        jdbcTemplate.update("insert into player(currnt_timestamp, player_name, full_name, phone) values(CURRENT_TIMESTAMP, ?, ?, ?)", player.getPlayerName(), player.getFullName(), player.getPhone());
    }

    public boolean isTablePlayersAlreadyExists(int tableId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        List<Integer> playerIds = namedParameterJdbcTemplate.query(
                "select player_id from table_player where table_id = :table_id", parameters,
                (rs, rowNum) -> rs.getInt("player_id"));

        return playerIds.size() > 0;

    }
    public void createTablePlayer(TablePlayer tablePlayer) {
        if(isTablePlayersAlreadyExists(tablePlayer.getTableId())){
            throw new IllegalArgumentException("table with players is already created");
        }
        tablePlayer.getPlayers().forEach(p -> {
            int playerId = getPlayerId(p);
            jdbcTemplate.update("insert into table_player(currnt_timestamp, table_id, player_id, is_playing) values(CURRENT_TIMESTAMP, ?, ?, ?)", tablePlayer.getTableId(), playerId, 1);
        });
    }

    public void createTableGame(TableGame tableGame) {
        if(tableGame.getBidAmount() < 0) {
            throw new IllegalArgumentException("bid amount can not be negative");
        }
        if(tableGame.getGameSequence() <= 0) {
            throw new IllegalArgumentException("invalid game sequence");
        }
        jdbcTemplate.update("insert into table_game(currnt_timestamp, table_id, game_sequence, bid_amount, is_running) values(CURRENT_TIMESTAMP, ?, ?, ?, ?)", tableGame.getTableId(), tableGame.getGameSequence(), tableGame.getBidAmount(), tableGame.isRunning() ? 1 : 0);
    }

    private List<Integer> getTablePlayers(int tableId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        List<Integer> playerIds = namedParameterJdbcTemplate.query(
                "select distinct player_id from table_player where table_id = :table_id", parameters,
                (rs, rowNum) -> rs.getInt("player_id"));

        return playerIds;

    }
    private void isEveryonePlayingOnTable(List<Integer> actualTablePlayerIds, List<Integer> receivedPlayerIds){
        if(actualTablePlayerIds.size() != receivedPlayerIds.size()){
            throw new IllegalArgumentException("Should provide the score for all the players who playing on the table");
        }
        for(int playerId :receivedPlayerIds){
            if(!actualTablePlayerIds.contains(playerId)){
                throw new IllegalArgumentException("received player ids are not playing on this table");
            }
        }
    }
    public void createTableGameScores(TableGamePlayerScore tableGamePlayerScore) {
        List<Integer> tablePlayers = getTablePlayers(tableGamePlayerScore.getTableId());
        isEveryonePlayingOnTable(tablePlayers, getPlayerIds(tableGamePlayerScore.getPlayerNames()));

        int gameId = getGameId(tableGamePlayerScore.getTableId(), tableGamePlayerScore.getGameSequence());
        jdbcTemplate.update("delete from table_game_player_score where table_id=? and game_id=?", tableGamePlayerScore.getTableId(), gameId);

        IntStream.range(0, tableGamePlayerScore.getPlayerNames().size()).forEach(idx -> {
            int playerId = getPlayerId(tableGamePlayerScore.getPlayerNames().get(idx));
            int score = tableGamePlayerScore.getScores().get(idx);
            jdbcTemplate.update("insert into table_game_player_score(currnt_timestamp, table_id, game_id, player_id, score) values(CURRENT_TIMESTAMP, ?, ?, ?, ?)", tableGamePlayerScore.getTableId(), gameId, playerId, score);
        });
    }

    private int getGameId(int tableId, int gameSequence) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        parameters.addValue("gameSequence", gameSequence);

        List<Integer> gameIds = namedParameterJdbcTemplate.query(
                "select game_id from table_game where table_id = :table_id and game_sequence = :gameSequence", parameters,
                (rs, rowNum) -> rs.getInt("game_id"));

        return gameIds.get(0);

    }

    public void createTableScores(TablePlayerTotalScore tablePlayerTotalScore) {
        List<Integer> tablePlayers = getTablePlayers(tablePlayerTotalScore.getTableId());
        isEveryonePlayingOnTable(tablePlayers, getPlayerIds(tablePlayerTotalScore.getPlayerNames()));

        jdbcTemplate.update("delete from table_player_score where table_id=?", tablePlayerTotalScore.getTableId());

        IntStream.range(0, tablePlayerTotalScore.getPlayerNames().size()).forEach(idx -> {
            int playerId = getPlayerId(tablePlayerTotalScore.getPlayerNames().get(idx));
            int score = tablePlayerTotalScore.getScores().get(idx);
            jdbcTemplate.update("insert into table_player_score(currnt_timestamp, table_id, player_id, score) values(CURRENT_TIMESTAMP, ?, ?, ?)", tablePlayerTotalScore.getTableId(), playerId, score);
        });

    }

    private boolean isBlank(String text) {
        return (text == null || text.trim().isEmpty());
    }
}

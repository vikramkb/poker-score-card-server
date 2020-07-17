package com.poker.score;

import com.poker.score.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class PokerService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    void createTable(Table table) {
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
            throw new IllegalArgumentException();
        }
        return playerIds.get(0);
    }

    public void createPlayer(Player player) {
        jdbcTemplate.update("insert into player(currnt_timestamp, player_name, full_name, phone) values(CURRENT_TIMESTAMP, ?, ?, ?)", player.getPlayerName(), player.getFullName(), player.getPhone());
    }

    public void createTablePlayer(TablePlayer tablePlayer) {
        tablePlayer.getPlayers().forEach(p -> {
            int playerId = getPlayerId(p);
            jdbcTemplate.update("insert into table_player(currnt_timestamp, table_id, player_id, is_playing) values(CURRENT_TIMESTAMP, ?, ?, ?)", tablePlayer.getTableId(), playerId, 1);
        });
    }

    public void createTableGame(TableGame tableGame) {
        jdbcTemplate.update("insert into table_game(currnt_timestamp, table_id, game_sequence, bid_amount, is_running) values(CURRENT_TIMESTAMP, ?, ?, ?, ?)", tableGame.getTableId(), tableGame.getGameSequence(), tableGame.getBidAmount(), tableGame.isRunning() ? 1 : 0);
    }

    public void createTableGameScores(TableGamePlayerScore tableGamePlayerScore) {
        IntStream.range(0, tableGamePlayerScore.getPlayerNames().size()).forEach(idx -> {
            int playerId = getPlayerId(tableGamePlayerScore.getPlayerNames().get(idx));
            int score = tableGamePlayerScore.getScores().get(idx);
            jdbcTemplate.update("insert into table_game_player_score(currnt_timestamp, table_id, game_id, player_id, score) values(CURRENT_TIMESTAMP, ?, ?, ?, ?)", tableGamePlayerScore.getTableId(), tableGamePlayerScore.getGameId(), playerId, score);
        });
    }

    public void createTableScores(TablePlayerTotalScore tablePlayerTotalScore) {
        IntStream.range(0, tablePlayerTotalScore.getPlayerNames().size()).forEach(idx -> {
            int playerId = getPlayerId(tablePlayerTotalScore.getPlayerNames().get(idx));
            int score = tablePlayerTotalScore.getScores().get(idx);
            jdbcTemplate.update("insert into table_player_score(currnt_timestamp, table_id, player_id, score) values(CURRENT_TIMESTAMP, ?, ?, ?)", tablePlayerTotalScore.getTableId(), playerId, score);
        });

    }
}

package com.poker.score;

import com.poker.score.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
public class PokerService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public int createTable(Table table) {
        if(table.getTableId() < 0) {
            throw new IllegalArgumentException("invalid table id");
        }
        int playerId = getPlayerId(table.getCreatedPlayerName());
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("table_name", table.getTableName())
                .addValue("created_player_id", playerId)
                .addValue("is_running", table.isRunning() ? 1 : 0);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update("insert into poker_table(currnt_timestamp, table_name, created_player_id, is_running) values(CURRENT_TIMESTAMP, :table_name, :created_player_id, :is_running)", namedParameters, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
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
        int order = 1;
        for(String p : tablePlayer.getPlayers()) {
            int playerId = getPlayerId(p);
            jdbcTemplate.update("insert into table_player(currnt_timestamp, table_id, player_id, is_playing, seating_order) values(CURRENT_TIMESTAMP, ?, ?, ?, ?)", tablePlayer.getTableId(), playerId, 1, order);
            order++;
        }
    }

    public int createTableGame(TableGame tableGame) {
        if(tableGame.getGameSequence() <= 0) {
            throw new IllegalArgumentException("invalid game sequence");
        }
        Integer winnerPlayerId = tableGame.getWinnerName() != null && !tableGame.getWinnerName().isEmpty() ? getPlayerId(tableGame.getWinnerName()) : null;

        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("table_id", tableGame.getTableId())
                .addValue("game_sequence", tableGame.getGameSequence())
                .addValue("winner_player_id", winnerPlayerId)
                .addValue("is_running", tableGame.isRunning() ? 1 : 0);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update("insert into table_game(currnt_timestamp, table_id, game_sequence, is_running, winner_player_id) values(CURRENT_TIMESTAMP, :table_id, :game_sequence, :is_running, :winner_player_id)", namedParameters, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
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

//        int gameId = getGameId(tableGamePlayerScore.getTableId(), tableGamePlayerScore.getGameSequence());
        int gameId = tableGamePlayerScore.getGameId();
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

        jdbcTemplate.update("delete from table_total_player_score where table_id=?", tablePlayerTotalScore.getTableId());

        IntStream.range(0, tablePlayerTotalScore.getPlayerNames().size()).forEach(idx -> {
            int playerId = getPlayerId(tablePlayerTotalScore.getPlayerNames().get(idx));
            int score = tablePlayerTotalScore.getScores().get(idx);
            jdbcTemplate.update("insert into table_total_player_score(currnt_timestamp, table_id, player_id, score) values(CURRENT_TIMESTAMP, ?, ?, ?)", tablePlayerTotalScore.getTableId(), playerId, score);
        });

    }

    private boolean isBlank(String text) {
        return (text == null || text.trim().isEmpty());
    }

    /*------------*/
    private Map<Integer, String>  getPlayerIdNameMap() {
        Map<Integer, String> playerIdNameMap = new HashMap<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        namedParameterJdbcTemplate.query(
                "select player_id, player_name from player", parameters,
                (rs, rowNum) -> {
                    playerIdNameMap.put(rs.getInt("player_id"), rs.getString("player_name"));
                    return 1;
                });

        return playerIdNameMap;
    }

    private Map<String, Integer>  getPlayerNameIdMap() {
        Map<String, Integer> playerNameIdMap = new HashMap<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        namedParameterJdbcTemplate.query(
                "select player_id, player_name from player", parameters,
                (rs, rowNum) -> {
                    playerNameIdMap.put(rs.getString("player_name"), rs.getInt("player_id"));
                    return 1;
                });

        return playerNameIdMap;
    }

    public FullTable getFullTable(int tableId) throws RESOURCE_NOT_FOUND_EXCEPTION {
        Map<Integer, String> playerIdNameMap = getPlayerIdNameMap();
        Table table = getTable(tableId, playerIdNameMap);
        TablePlayer tablePlayer = getTablePlayer(tableId, playerIdNameMap);
        List<TableGame> tableGames = getTableGames(tableId, playerIdNameMap);
        List<TableGamePlayerScore> tableGamePlayerScores = getTableGamePlayerScore(tableId, playerIdNameMap);
        TablePlayerTotalScore tablePlayerTotalScore = getTableScore(tableId, playerIdNameMap);
        List<TableGameRoundPlayer> tableGameRoundPlayers = getTableGameRoundPlayers(tableId);

        FullTable fullTable = new FullTable();
        fullTable.setTable(table);
        fullTable.setGames(tableGames);
        fullTable.setGamesScore(tableGamePlayerScores);
        fullTable.setTableTotalScore(tablePlayerTotalScore);
        fullTable.setPlayers(tablePlayer);
        fullTable.setTableGameRoundPlayers(tableGameRoundPlayers);

        return fullTable;
    }

    private List<TableGameRoundPlayer> getTableGameRoundPlayers(int tableId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        List<TableGameRoundPlayer> tableGameRoundPlayers = namedParameterJdbcTemplate.query(
                "SELECT tgrp.table_id, tgrp.game_id, tgrp.round_id, tgr.bid_amount, GROUP_CONCAT(p.player_name ORDER BY tgrp.table_id, tgrp.game_id, tgrp.round_id, tgrp.seating_order asc) round_players FROM table_game_round tgr, table_game_round_player tgrp, player p where tgr.round_ID=tgrp.round_id and tgrp.player_id=p.player_id and tgr.table_id = :table_id group by table_id, game_id, round_id, bid_amount order by table_id, game_id, round_id;", parameters,
                (rs, rowNum) -> {
                    TableGameRoundPlayer round = new TableGameRoundPlayer();
                    round.setTableId(rs.getInt("table_id"));
                    round.setGameId(rs.getInt("game_id"));
                    round.setRoundId(rs.getInt("round_id"));
                    round.setBidAmount(rs.getInt("bid_amount"));
                    round.setPlayerNames(Arrays.asList(rs.getString("round_players").split(",")));
                    return round;
                });
        return tableGameRoundPlayers;
    }

    public Table getTable(int tableId, Map<Integer, String> playerIdNameMap) throws RESOURCE_NOT_FOUND_EXCEPTION {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        List<Table> tables = namedParameterJdbcTemplate.query(
                "select * from poker_table where table_id = :table_id", parameters,
                (rs, rowNum) -> {
                    Table table = new Table();
                    table.setTableId(rs.getInt("table_id"));
                    table.setRunning(rs.getInt("is_running") == 1);
                    table.setTableName(rs.getString("table_name"));
                    table.setCreatedPlayerName(playerIdNameMap.get(rs.getInt("created_player_id")));
                    table.setTimestamp(rs.getTimestamp("currnt_timestamp"));
                    return table;
                });
        if (tables.size() == 0) {
            throw new RESOURCE_NOT_FOUND_EXCEPTION();
        }
        return tables.get(0);
    }

    public TablePlayer getTablePlayer(int tableId, Map<Integer, String> playerIdNameMap) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        TablePlayer tablePlayer = new TablePlayer();
        tablePlayer.setTableId(tableId);
        List<String> playerNames = new ArrayList<>();
        namedParameterJdbcTemplate.query(
                "select * from table_player where table_id = :table_id order by seating_order asc", parameters,
                (rs, rowNum) -> {
                    playerNames.add(playerIdNameMap.get(rs.getInt("player_id")));
                    return 1;
                });
        tablePlayer.setPlayers(playerNames);
        return tablePlayer;
    }

    public Optional<Integer> getRoundId(int tableId, int gameId, int roundSequence) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        parameters.addValue("game_id", gameId);
        parameters.addValue("round_sequence", roundSequence);
        List<Integer> roundId = namedParameterJdbcTemplate.query(
                "select round_id from table_game_round where table_id = :table_id and game_id = :game_id and round_sequence = :round_sequence", parameters,
                (rs, rowNum) -> rs.getInt("round_id"));

        return roundId.size() > 0 ? Optional.of(roundId.get(0)) : Optional.empty();
    }

    public List<TableGame> getTableGames(int tableId, Map<Integer, String> playerIdNameMap) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        List<TableGame> tableGames = namedParameterJdbcTemplate.query(
                "select * from table_game where table_id = :table_id", parameters,
                (rs, rowNum) -> {
                    TableGame tableGame = new TableGame();
                    tableGame.setGameId(rs.getInt("game_id"));
                    tableGame.setWinnerName(playerIdNameMap.get(rs.getInt("winner_player_id")));
                    tableGame.setGameSequence(rs.getInt("game_sequence"));
                    tableGame.setRunning(rs.getInt("is_running") == 1);
                    tableGame.setTableId(rs.getInt("table_id"));
                    tableGame.setTimestamp(rs.getTimestamp("currnt_timestamp"));
                    return tableGame;
                });
        return tableGames;
    }

    private List<Pair<Integer, Integer>> getTableGameIds(int tableId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);

        List<Pair<Integer, Integer>> gameIds = namedParameterJdbcTemplate.query(
                "select game_id, game_sequence from table_game where table_id = :table_id order by game_sequence", parameters,
                (rs, rowNum) -> {
                    Pair<Integer, Integer> gameidSeq = new Pair<>();
                    gameidSeq.setFirst(rs.getInt("game_id"));
                    gameidSeq.setSecond(rs.getInt("game_sequence"));
                   return gameidSeq;
                });

        return gameIds;
    }
    private List<TableGamePlayerScore> getTableGamePlayerScore(int tableId, Map<Integer, String> playerIdNameMap) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
        List<TableGamePlayerScore> tableGamePlayerScores = new ArrayList<>();
        getTableGameIds(tableId).stream().forEach(g -> {
            TableGamePlayerScore tableGamePlayerScore = new TableGamePlayerScore();
            tableGamePlayerScore.setGameSequence(g.getSecond());
            tableGamePlayerScore.setGameId(g.getFirst());
            tableGamePlayerScore.setTableId(tableId);
            List<String> playerNames = new ArrayList<>();
            List<Integer> scores = new ArrayList<>();

            MapSqlParameterSource parameters1 = new MapSqlParameterSource();
            parameters1.addValue("table_id", tableId);
            parameters1.addValue("game_id", g.getFirst());

            namedParameterJdbcTemplate.query(
                    "select * from table_game_player_score where table_id = :table_id and game_id = :game_id", parameters1,
                    (rs, rowNum) -> {
                        playerNames.add(playerIdNameMap.get(rs.getInt("player_id")));
                        scores.add(rs.getInt("score"));
                        return 1;
                    });
            tableGamePlayerScore.setPlayerNames(playerNames);
            tableGamePlayerScore.setScores(scores);
            tableGamePlayerScores.add(tableGamePlayerScore);
        });

        return tableGamePlayerScores;
    }
    private TablePlayerTotalScore getTableScore(int tableId, Map<Integer, String> playerIdNameMap) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("table_id", tableId);
            TablePlayerTotalScore tablePlayerTotalScore = new TablePlayerTotalScore();
            tablePlayerTotalScore.setTableId(tableId);
            List<String> playerNames = new ArrayList<>();
            List<Integer> scores = new ArrayList<>();

            MapSqlParameterSource parameters1 = new MapSqlParameterSource();
            parameters1.addValue("table_id", tableId);

            namedParameterJdbcTemplate.query(
                    "select * from table_total_player_score where table_id = :table_id", parameters,
                    (rs, rowNum) -> {
                        playerNames.add(playerIdNameMap.get(rs.getInt("player_id")));
                        scores.add(rs.getInt("score"));
                        return 1;
                    });
        tablePlayerTotalScore.setPlayerNames(playerNames);
        tablePlayerTotalScore.setScores(scores);

        return tablePlayerTotalScore;
    }

    public List<Player> getPlayers() {
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        List<Player> players = namedParameterJdbcTemplate.query(
                "select * from player", parameters,
                (rs, rowNum) -> {
                    Player player = new Player();
                    player.setFullName(rs.getString("full_name"));
                    player.setPhone(rs.getString("phone"));
                    player.setPlayerName(rs.getString("player_name"));
                    return player;
                });

        return players;
    }

    public List<TableScore> getTables() {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        Map<Integer, String> playerIdNameMap = getPlayerIdNameMap();
        List<TableScore> tables = namedParameterJdbcTemplate.query(
                "select * from poker_table order by currnt_timestamp desc", parameters,
                (rs, rowNum) -> {
                    Table table = new Table();
                    table.setTableId(rs.getInt("table_id"));
                    table.setRealGame(rs.getInt("is_real_game") == 1);
                    table.setTableName(rs.getString("table_name"));
                    table.setCreatedPlayerName(playerIdNameMap.get(rs.getString("created_player_id")));
                    table.setTimestamp(rs.getTimestamp("currnt_timestamp"));
                    table.setRunning(rs.getInt("is_running") == 1);
                    TablePlayerTotalScore tablePlayerTotalScore = getTableScore(rs.getInt("table_id"), playerIdNameMap);
                    TableScore tableScore = new TableScore();
                    tableScore.setTable(table);
                    tableScore.setTablePlayerTotalScore(tablePlayerTotalScore);
                    return tableScore;
                });

        return tables;
    }

    public void updateGameStatus(int tableId, int gameId, boolean isRunning, String winnerName) {
        int winnerPlayerId = getPlayerId(winnerName);
        jdbcTemplate.update("update table_game set is_running = ?, winner_player_id =? where table_id = ? and game_id = ?", isRunning ? 1 : 0, winnerPlayerId, tableId, gameId);
    }

    public void updateTableStatus(int tableId, boolean isRunning) {
        jdbcTemplate.update("update poker_table set is_running = ? where table_id = ?", isRunning ? 1 : 0, tableId);
    }

    public int createTableGameRound(TableGameRoundPlayer tableGameRoundPlayer) {
        if(tableGameRoundPlayer.getRoundSequence() < 0 || tableGameRoundPlayer.getBidAmount() < 0){
            throw new IllegalArgumentException();
        }

        Optional<Integer> roundId = getRoundId(tableGameRoundPlayer.getTableId(), tableGameRoundPlayer.getGameId(), tableGameRoundPlayer.getRoundSequence());
        roundId.map(r -> {
            jdbcTemplate.update("delete from table_game_round_player where table_id=? and game_id=? and round_id=?",tableGameRoundPlayer.getTableId(),tableGameRoundPlayer.getGameId(),r);
            jdbcTemplate.update("delete from table_game_round where table_id=? and game_id=? and round_id=?",tableGameRoundPlayer.getTableId(),tableGameRoundPlayer.getGameId(),r);
            return r;
        });
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("table_id", tableGameRoundPlayer.getTableId())
                .addValue("game_id", tableGameRoundPlayer.getGameId())
                .addValue("round_sequence", tableGameRoundPlayer.getRoundSequence())
                .addValue("bid_amount", tableGameRoundPlayer.getBidAmount());
        namedParameterJdbcTemplate.update("insert into table_game_round(currnt_timestamp, table_id, game_id, round_sequence, bid_amount) values(CURRENT_TIMESTAMP, :table_id, :game_id, :round_sequence, :bid_amount)", namedParameters, keyHolder);
        Map<String, Integer> playerNameIdMap = getPlayerNameIdMap();
        int order = 1;
        for(String p : tableGameRoundPlayer.getPlayerNames()) {
            jdbcTemplate.update("insert into table_game_round_player(currnt_timestamp, table_id, game_id, round_id, player_id, seating_order) values(CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)", tableGameRoundPlayer.getTableId(), tableGameRoundPlayer.getGameId(), keyHolder.getKey(),  playerNameIdMap.get(p), order);
            order++;
        }
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }
}

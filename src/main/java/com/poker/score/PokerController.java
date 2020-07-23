package com.poker.score;

import com.poker.score.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PokerController {
    @Autowired
    private PokerService pokerService;

    @RequestMapping(value = "/table", method = RequestMethod.POST)
    public ResponseEntity<Integer> createTableGame(@RequestBody Table table){
        int tableId = pokerService.createTable(table);
        return new ResponseEntity<>(tableId, HttpStatus.OK);
    }

    @RequestMapping(value = "/table/player", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayer(@RequestBody TablePlayer tablePlayer){
        pokerService.createTablePlayer(tablePlayer);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/table/game", method = RequestMethod.POST)
    public ResponseEntity<Integer> createTableGame(@RequestBody TableGame tableGame){
        int gameId = pokerService.createTableGame(tableGame);
        return new ResponseEntity<>(gameId, HttpStatus.OK);
    }

    @RequestMapping(value = "/player", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayer(@RequestBody Player player){
        pokerService.createPlayer(player);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/players", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayers(@RequestBody List<Player> players){
        players.forEach(p -> {
            pokerService.createPlayer(p);
        });
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/table/game/scores", method = RequestMethod.POST)
    public ResponseEntity<String> tableGameScore(@RequestBody TableGamePlayerScore tableGamePlayerScore){
        pokerService.createTableGameScores(tableGamePlayerScore);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/table/game/round", method = RequestMethod.POST)
    public ResponseEntity<Integer> tableGameRoundPlayers(@RequestBody TableGameRoundPlayer tableGamePlayerScore){
        int roundId = pokerService.createTableGameRound(tableGamePlayerScore);
        return new ResponseEntity<>(roundId, HttpStatus.OK);
    }

    @RequestMapping(value = "/table/player/scores", method = RequestMethod.POST)
    public ResponseEntity<String> tableGameScore(@RequestBody TablePlayerTotalScore tablePlayerTotalScore){
        pokerService.createTableScores(tablePlayerTotalScore);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/table", method = RequestMethod.GET)
    public ResponseEntity<FullTable> fullTable(@RequestParam int tableId) throws RESOURCE_NOT_FOUND_EXCEPTION {
        FullTable fullTable = pokerService.getFullTable(tableId);
        return new ResponseEntity<>(fullTable, HttpStatus.OK);
    }

    @RequestMapping(value = "/players", method = RequestMethod.GET)
    public ResponseEntity<List<Player>> getPlayers(){
        List<Player> players = pokerService.getPlayers();
        return new ResponseEntity<>(players, HttpStatus.OK);
    }

    @RequestMapping(value = "/table/game/status", method = RequestMethod.POST)
    public ResponseEntity<String> updateGameStatus(@RequestBody TableUpdateInfo tableUpdateInfo){
        pokerService.updateGameStatus(tableUpdateInfo.getTableId(), tableUpdateInfo.getGameId(), tableUpdateInfo.isRunning());
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/table/status", method = RequestMethod.POST)
    public ResponseEntity<String> updateTableStatus(@RequestBody TableUpdateInfo tableUpdateInfo){
        pokerService.updateTableStatus(tableUpdateInfo.getTableId(), tableUpdateInfo.isRunning());
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/tables", method = RequestMethod.GET)
    public ResponseEntity<List<TableScore>> getTables(){
        List<TableScore> tables = pokerService.getTables();
        return new ResponseEntity<>(tables, HttpStatus.OK);
    }

    @RequestMapping(value = "/full-tables", method = RequestMethod.GET)
    public ResponseEntity<List<FullTable>> fullTable() throws RESOURCE_NOT_FOUND_EXCEPTION {
        List<TableScore> tables = pokerService.getTables();
        List<FullTable> fullTables = tables.stream().map(t -> {
            try {
                return pokerService.getFullTable(t.getTable().getTableId());
            } catch (RESOURCE_NOT_FOUND_EXCEPTION resource_not_found_exception) {
                resource_not_found_exception.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());

        return new ResponseEntity<>(fullTables, HttpStatus.OK);
    }


}

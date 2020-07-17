package com.poker.score;

import com.poker.score.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PokerController {
    @Autowired
    private PokerService pokerService;

    @RequestMapping(value = "/table", method = RequestMethod.POST)
    public ResponseEntity<String> createTableGame(@RequestBody Table table){
        pokerService.createTable(table);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/table/player", method = RequestMethod.POST)
    public ResponseEntity<String> createPlayer(@RequestBody TablePlayer tablePlayer){
        pokerService.createTablePlayer(tablePlayer);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "/table/game", method = RequestMethod.POST)
    public ResponseEntity<String> createTableGame(@RequestBody TableGame tableGame){
        pokerService.createTableGame(tableGame);
        return new ResponseEntity<>("ok", HttpStatus.OK);
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

    @RequestMapping(value = "/table/player/scores", method = RequestMethod.POST)
    public ResponseEntity<String> tableGameScore(@RequestBody TablePlayerTotalScore tablePlayerTotalScore){
        pokerService.createTableScores(tablePlayerTotalScore);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

}

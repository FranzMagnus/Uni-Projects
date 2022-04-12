package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.typesafe.config.ConfigException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import models.DatabaseManager;
import models.Location;
import models.Match;
import models.Player;
import models.User;
import models.communication.ClientConnect;
import models.communication.Done;
import models.communication.ExerciseData;
import models.communication.GameData;
import models.communication.NewPosition;
import models.communication.Packet;
import models.communication.Ranking;
import models.communication.Ready;
import models.communication.RoundData;
import models.communication.UserData;
import models.communication.Winner;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.WebSocket;

public class GameController extends Controller {

    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final DatabaseManager databaseManager;

    private Map<String, Map<Integer, Set<String>>> matches;
    private Map<String, MyWebSocketActor> clients;

    private Map<String, Integer> exercisesDone; // TODO: maybe in Datenbank speichern?

    @Inject
    public GameController(ActorSystem actorSystem, Materializer materializer, DatabaseManager databaseManager) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.databaseManager = databaseManager;

        matches = new HashMap<String, Map<Integer, Set<String>>>();
        clients = new HashMap<String, MyWebSocketActor>();

        exercisesDone = new HashMap<String, Integer>();
    }

    public WebSocket socket() {
        WebSocket wS = WebSocket.Text.accept(
                request -> ActorFlow.actorRef((ref) -> MyWebSocketActor.props(ref, this), actorSystem, materializer));
        return wS;
    }

    public void receive(String message, MyWebSocketActor sender) {
        System.out.println("Received in GameController: " + message);
        handleMessage(message, sender);
    }

    private void handleMessage(String message, MyWebSocketActor sender) {
        try {
            Packet p = new Packet(message);

            if (p.type == 0) {
                try {
                    handleClientConnect((ClientConnect) p.value, sender);
                }catch(NullPointerException e) {
                    e.printStackTrace();
                }
            } else if (p.type == 2) {
                handlePlayerReady((Ready) p.value, sender);
            } else if (p.type == 4) {
                handlePlayerDone((Done) p.value, sender);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, Set<String>> getMatchMap(String matchID) {
        Map<Integer, Set<String>> map = matches.get(matchID);
        if (map == null) {
            map = new HashMap<Integer, Set<String>>();
            matches.put(matchID, map);
        }
        return map;
    }

    private Set<String> getMatchPlayers(String matchID) {
        Map<Integer, Set<String>> match = getMatchMap(matchID);
        Set<String> players = new HashSet<String>();
        for (Map.Entry<Integer, Set<String>> e : match.entrySet()) {
            players.addAll(e.getValue());
        }
        return players;
    }

    private Set<String> getTeam(String matchID, int teamID) {
        Map<Integer, Set<String>> match = getMatchMap(matchID);
        return match.get(teamID);
    }

    private void insertIntoTeam(String matchID, int teamID, String firebaseID) {
        Map<Integer, Set<String>> match = getMatchMap(matchID);
        Set<String> team = match.get(teamID);
        if (team == null) {
            team = new HashSet<String>();
            match.put(teamID, team);
        }
        team.add(firebaseID);
    }

    private GameData createGameData(String matchID, int teamID, Player player) {
        Match matchInfo = databaseManager.findMatch(matchID);

        Location[] locations = matchInfo.getLocations();

        String[] players = matchInfo.getPlayers();

        UserData[] users = new UserData[players.length];
        for (int i = 0; i < players.length; i++) {
            Player p = databaseManager.findPlayer(players[i]);
            User u = databaseManager.findUser(players[i]);

            users[i] = new UserData(u.getUsername(), p.getTeamID());
        }

        return new GameData(locations, users, teamID, matchInfo.getTeamCount(), player.isJoker(), matchInfo.getDifficulty(), player.getCurrent_loc_index());
    }

    private void sendToMatch(String matchID, String message) {
        Set<String> players = getMatchPlayers(matchID);
        for (String s : players) {
            sendToClient(s, message);
        }
    }

    private void sendToTeam(String matchID, int teamID, String message) {
        Set<String> team = getTeam(matchID, teamID);
        for (String s : team) {
            sendToClient(s, message);
        }
    }

    private void sendToClient(String id, String message) {
        System.out.println("To " + id + ": " + message);
        clients.get(id).send(message);
    }

    public void handleClientConnect(ClientConnect cC, MyWebSocketActor sender) throws NullPointerException{
        System.out.println(cC.getFirebaseID() + " connected");

        // save client connection
        clients.put(cC.getFirebaseID(), sender);

        // find matchID and teamID for the firebaseID
        Player p = databaseManager.findPlayer(cC.getFirebaseID());
        String matchID = p.getHostID();
        int teamID = p.getTeamID();

        insertIntoTeam(matchID, teamID, cC.getFirebaseID());

        // send the player the match information
        GameData gD = createGameData(matchID, teamID, p);
        try {
            sendToClient(cC.getFirebaseID(), Packet.createPacket(gD));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // check if this client is connecting while the match is running and send current positions
        Match m = databaseManager.findMatch(matchID);
        if (m.getStatus() == 1) {
            for (int i = 0; i < m.getTeamCount(); i++) {
                Set<String> team = getTeam(matchID, i);
                int teamPlace = databaseManager.findPlayer(team.iterator().next()).getCurrent_loc_index();
                try {
                    sendToClient(cC.getFirebaseID(), Packet.createPacket(new NewPosition(teamPlace, i)));
                } catch (JsonProcessingException e) {
                    // save player into team
                    e.printStackTrace();
                }
            }

            // check whether client was supposed to do exercises
            if(exercisesDone.get(cC.getFirebaseID()) == -1) {
                // just say he did 0 to finish the round
                handlePlayerDone(new Done(cC.getFirebaseID(), 0), sender);
            }
        }
    }

    public void handlePlayerReady(Ready ready, MyWebSocketActor sender) {
        System.out.println(ready.getFirebaseID() + " ready");

        // get matchID and teamID from firebaseID
        Player p = databaseManager.findPlayer(ready.getFirebaseID());
        String matchID = p.getHostID();
        int teamID = p.getTeamID();

        // set player to ready
        databaseManager.updatePlayer_setReadyTrue(ready.getFirebaseID());

        // check match status
        Match match = databaseManager.findMatch(matchID);
        if (match.getStatus() == 0) {
            String[] pls = match.getPlayers();
            for (String s : pls) {
                if (clients.get(s) == null) {
                    return;
                }
            }

            // every player needs to be ready (synchronized start)
            Set<String> players = getMatchPlayers(matchID);

            // if everyone is ready
            for (String s : players) {
                Player pl = databaseManager.findPlayer(s);
                if (!pl.getReady()) {
                    return;
                }
            }

            for (int i = 0; i < match.getTeamCount(); i++) {
                try {
                    sendToTeam(matchID, i, Packet.createPacket(generateRound(match.getDifficulty())));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            for (String s : players) {
                exercisesDone.put(s, -1);
                databaseManager.updatePlayer_setReadyFalse(s);
                databaseManager.updatePlayer_incTurnNumber(s);
            }

            databaseManager.updateMatch_changeStatus(matchID);
        } else if (match.getStatus() == 1) {

            Set<String> team = getTeam(matchID, teamID);
            for (String s : team) {
                Player pl = databaseManager.findPlayer(s);
                if (!pl.getReady()) {
                    return;
                }
            }

            // check if team won
            System.out.println(p.getCurrent_loc_index());
            if (p.getCurrent_loc_index() == match.getLocations().length - 1) {
                teamWon(matchID, teamID);
                return;
            }

            // if not won, next exercise
            try {
                sendToTeam(matchID, teamID, Packet.createPacket(generateRound(match.getDifficulty())));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            for (String s : team) {
                exercisesDone.put(s, -1);
                databaseManager.updatePlayer_setReadyFalse(s);
                databaseManager.updatePlayer_incTurnNumber(s);
            }
        }
    }

    private void teamWon(String matchID, int teamID) {
        try {
            sendToMatch(matchID, Packet.createPacket(new Winner(teamID)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Set<String> team = getTeam(matchID, teamID);
        for (String s : team) {
            databaseManager.updatePlayer_setFinishedTrue(s);
        }

        Match m = databaseManager.findMatch(matchID);
        String[] players = m.getPlayers();
        boolean[] teamDone = new boolean[m.getTeamCount()];
        int[] roundNumber = new int[m.getTeamCount()];
        for (String s : players) {
            Player p = databaseManager.findPlayer(s);
            if (p.isFinished()) {
                teamDone[p.getTeamID()] = true;
                roundNumber[p.getTeamID()] = p.getTurn_number();
            }
        }

        for (int i = 0; i < teamDone.length; i++) {
            if (!teamDone[i]) {
                return;
            }
        }

        // everyone is done!
        try {
            sendToMatch(matchID, Packet.createPacket(new Ranking(roundNumber)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        databaseManager.updateMatch_changeStatus(matchID);

        // TODO: save stats (winner ...)
        saveStats(matchID);

        // delete match and players from database and connections
        databaseManager.removeMatch(matchID);
        matches.remove(matchID);
        for(String s : players) {
            databaseManager.removePlayer(s);
            clients.remove(s);
        }
    }

    //Increases # of Games played with teammate and adds Joker if possible
    private void saveStats(String matchID){
        Collection<Set<String>> values = getMatchMap(matchID).values();
        for(Set<String> team : values){
           for(String s1 : team){
               for(String s2 :team){
                   if(!s1.equals(s2)){
                       if(databaseManager.increasePlayedWith(s1,s2,1) % 10 ==0){
                           databaseManager.increaseJokersWith(s1,s2,3);
                       }
                   }
               }
           }
        }
    }

    private RoundData generateRound(double difficulty) {
        ExerciseData[] exercises = new ExerciseData[5];
        for (int i = 0; i < 5; i++) {
            exercises[i] = generateExercise(difficulty);
        }
        int time = 60;
        return new RoundData(exercises, time);
    }

    private ExerciseData generateExercise(double difficulty) {
        int exerciseID = (int) (Math.random() * 4);
        int count = (int) (10*difficulty);
        if (exerciseID == 3) {
            count = (int) (20*difficulty);
        }
        return new ExerciseData(exerciseID, count);
    }

    public void handlePlayerDone(Done done, MyWebSocketActor sender) {
        System.out.println(done.getFirebaseID() + " done");

        // get matchID and teamID with firebaseID
        Player p = databaseManager.findPlayer(done.getFirebaseID());
        String matchID = p.getHostID();
        int teamID = p.getTeamID();

        exercisesDone.put(done.getFirebaseID(), done.getCountDone());

        Set<String> team = getTeam(matchID, teamID);
        int sum = 0;
        for (String s : team) {
            int temp = exercisesDone.get(s);
            if (temp == -1) {
                return;
            }
            sum += temp;
        }
        int steps = sum / getTeam(matchID, teamID).size();

        int nextPlace = p.getCurrent_loc_index() + steps;

        Match m = databaseManager.findMatch(matchID);
        if (nextPlace >= m.getLocations().length) {
            nextPlace = m.getLocations().length - 1;
        }
        for (String s : team) {
            databaseManager.updatePlayer_changeLocationIndex(s, nextPlace);
        }

        try {
            sendToMatch(matchID, Packet.createPacket(new NewPosition(nextPlace, teamID)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}

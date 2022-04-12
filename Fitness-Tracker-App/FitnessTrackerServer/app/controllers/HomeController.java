package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

import models.Location;
import models.Match;
import models.Player;
import models.User;
import models.DatabaseManager;
import play.api.Play;
import play.mvc.*;


public class HomeController extends Controller {

    //creating a new Database-Manager
    DatabaseManager dbm = new DatabaseManager();
    ObjectMapper mapper = new ObjectMapper(); //Used for Json <-> Object conversion

    //-----------------------------------
    // INSERT-OPERATIONS
    //-----------------------------------


    public Result insert_User() {
        Http.Request request = request();
        try {
            User u = mapper.treeToValue(request.body().asJson(), User.class);
            dbm.insertUser(u);
            return ok();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    public Result insert_Player() {
        Http.Request request = request();
        try {
            Player p = mapper.treeToValue(request.body().asJson(), Player.class);
            dbm.insertPlayer(p);
            return ok();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return badRequest();
        }
    }



    public Result insert_Match() {
        Http.Request request = request();
        try {
            JsonNode json = request.body().asJson();
            System.out.println(json.toString());
            Match m = mapper.treeToValue(json, Match.class);
            dbm.insertMatch(m);
            return ok();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    public Result create_Match() {
        Http.Request request = request();
        try {
            JsonNode json = request.body().asJson();
            System.out.println(json.toString());
            Match m = mapper.treeToValue(json, Match.class);

            // check if host still has a match
            Match old = dbm.findMatch(m.getHostID());
            if(old != null) {
                dbm.removeMatch(old.getHostID());
                String[] oldPlayers = old.getPlayers();
                for(String s : oldPlayers) {
                    dbm.removePlayer(s);
                }
            }

            // check if user exists and map username to id
            String[] newPlayers = m.getPlayers();
            String[] playerIDs = new String[newPlayers.length];
            for(int i = 0;i<newPlayers.length;i++) {
                User u = dbm.findUserByName(newPlayers[i]);
                if(u == null) {
                    return badRequest("User " + newPlayers[i] + " does not exist");
                }
                playerIDs[i] = u.getFirebaseID();
            }
            m.setPlayers(playerIDs);

            // check if player is still in game
            for(String s : playerIDs) {
                Player p = dbm.findPlayer(s);
                if(p != null) {
                    return badRequest("Player " + s + " is still in game");
                }
            }

            //Checks Jokers for correctness
            for(int i = 0; i< m.getJokers().length;i++){
                if(m.getJokers()[i]){   //if Joker flag is set ->check it
                    int teamNum = m.getPlayerTeam()[i];
                    String partner = null;
                    for(int j = 0;j<m.getJokers().length && partner == null;j++){   //Find corresponding Teammate
                        if (m.getPlayerTeam()[j] == teamNum && j!=i){
                            partner = m.getPlayers()[j];

                        }
                    }
                    if(partner == null){        //No teammate found
                        return badRequest("No teammate found! Please assign 2 players per team");
                    }
                    User mate = dbm.findUser(partner);
                    Integer jokerCount = mate.getJokersWith().get(m.getPlayers()[i]);
                    if(jokerCount == null || jokerCount <=0){
                        return badRequest("User "+mate.getUsername()+ " does not have "+newPlayers[i]+" as a valid joker or JokerCount is 0");
                    }
                    //Deduct 1 Joker count from each Users Jokers
                    dbm.updateBothUsers_increaseJokersWith(m.getPlayers()[i],mate.getFirebaseID(),-1);
                }
            }

            dbm.insertMatch(m);

            for(int i = 0;i<m.getPlayers().length;i++) {
                Player p = new Player(m.getPlayers()[i],m.getHostID(),m.getPlayerTeam()[i],m.getJokers()[i]);
                dbm.insertPlayer(p);
            }
            return ok();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return badRequest();
        }
    }

    //-----------------------------------
    // FIND-OPERATIONS
    //-----------------------------------
    //[WORKING]
    public Result find_User(String firebaseID) throws JsonProcessingException {
        User foundUser = dbm.findUser(firebaseID);
        if(foundUser != null) {
            String userJson = mapper.writeValueAsString(foundUser);
            return ok(userJson);
        }else{
            //TODO Error handling / what if no target found?
            return ok("[FAILURE] User not found!");
        }
    }

    public Result find_Player(String firebaseID) throws JsonProcessingException {
        Player foundPlayer = dbm.findPlayer(firebaseID);
        if(foundPlayer != null){
            String playerJson = mapper.writeValueAsString(foundPlayer);
            return ok(playerJson);
        }else{
            //TODO Error handling / what if no target found?
            return ok("[FAILURE] Player not found!");
        }
    }

    public Result find_Match(String matchID) throws JsonProcessingException {
        Match foundMatch = dbm.findMatch(matchID);
        if(foundMatch != null){
            String matchJason = mapper.writeValueAsString(foundMatch);
            return ok(matchJason);
        }else{
            //TODO Error handling / what if no target found?
            return ok("[FAILURE] Match not found!");
        }
    }

    //-----------------------------------
    // DELETING-OPERATIONS
    //-----------------------------------
    public Result remove_User(String firebaseID){
        dbm.removeUser(firebaseID);
        return ok("[SUCCESS] User removed from collection!");
    }

    public Result remove_Player(String firebaseID){
        dbm.removePlayer(firebaseID);
        return ok("[SUCCESS] Player removed from collection!");
    }

    public Result remove_Match(String hostID){
        dbm.removeMatch(hostID);
        return ok("[SUCCESS] Match and all players removed from collections!");
    }

    //-----------------------------------
    // UPDATING USERS
    //-----------------------------------
    public Result user_updateName(String firebaseID, String name){
        dbm.updateUser_changeUsername(firebaseID, name);
        return ok("[SUCCESS] Username updated");
    }

    public Result user_incMatches(String firebaseID){
        dbm.updateUser_incMatches(firebaseID);
        return ok("[SUCCESS] Match number incremented");
    }

    public Result user_incWins(String firebaseID){
        dbm.updateUser_incWins(firebaseID);
        return ok("[SUCCESS] Win number incremented");
    }

    public Result user_addFriend(String firebaseID_user, String firebaseID_friend){
        dbm.updateUser_addFriend(firebaseID_user, firebaseID_friend);
        return ok("[SUCCESS] Friend added");
    }

    public Result user_removeFriend(String firebaseID_user, String firebaseID_friend){
        dbm.updateUser_removeFriend(firebaseID_user, firebaseID_friend);
        return ok("[SUCCESS] Friend removed");
    }

    public Result user_addRival(String firebaseID_user, String firebaseID_rival){
        dbm.updateUser_addRival(firebaseID_user, firebaseID_rival);
        return ok("[SUCCESS] Rival added");
    }

    public Result user_removeRival(String firebaseID_user, String firebaseID_rival){
        dbm.updateUser_removeRival(firebaseID_user, firebaseID_rival);
        return ok("[SUCCESS] Rival removed");
    }

    //-----------------------------------
    // UPDATING PLAYERS
    //-----------------------------------
    public Result player_incTurnNumber(String firebaseID){
        dbm.updatePlayer_incTurnNumber(firebaseID);
        return ok("[SUCCESS] Updated number of turns");
    }

    public Result player_updateLocation(String firebaseID, int location_index){
        dbm.updatePlayer_changeLocationIndex(firebaseID, location_index);
        return ok("[SUCCESS] Updated the Location of the player");
    }

    public Result player_setReadyTrue(String firebaseID){
        dbm.updatePlayer_setReadyTrue(firebaseID);
        return ok("[SUCCESS] Set Player to ready: TRUE");
    }

    public Result player_setReadyFalse(String firebaseID){
        dbm.updatePlayer_setReadyTrue(firebaseID);
        return ok("[SUCCESS] Set Player to ready: FALSE");
    }

    //-----------------------------------
    // UPDATING MATCHES
    //-----------------------------------
    public Result match_setWinners(String matchID, String firebaseID_winner1, String firebaseID_winner2){
        dbm.updateMatch_setWinners(matchID, firebaseID_winner1, firebaseID_winner2);
        return ok("[SUCCESS] Set winners for the match");
    }

    public Result match_changeStatus(String matchID){
        dbm.updateMatch_changeStatus(matchID);
        return ok("[SUCCESS Updates the status of the match");
    }
    //-----------------------------------
    // test functions
    //-----------------------------------
    public Result index() {
        return ok("Ok");
    }

    public Result test() {
        return ok("we have a connection!");
    }

    public Result isIngame(String firebaseID) {
        Player p = dbm.findPlayer(firebaseID);
        if(p != null) {
            return ok();
        }else {
            return status(201);
        }
    }

}

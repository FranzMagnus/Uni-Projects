package models;

//javax

import javax.inject.Singleton;
import javax.inject.*;

//play framework
import play.mvc.Controller;
import play.Logger;

//jongo
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import uk.co.panaxiom.playjongo.*;

//mongoDB
import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class DatabaseManager {
    //injecting PlayJongo to access database
    //@Inject
    //private PlayJongo jongo;

    DB database = new MongoClient().getDB("sg31");
    Jongo jongo = new Jongo(database);

    //-----------------------------------
    // getting the collections
    //-----------------------------------
    private MongoCollection user_collection() {
        MongoCollection user_collection = jongo.getCollection("users");
        return user_collection;
    }

    private MongoCollection player_collection() {
        MongoCollection player_collection = jongo.getCollection("players");
        return player_collection;
    }

    private MongoCollection match_collection() {
        MongoCollection match_collection = jongo.getCollection("matches");
        return match_collection;
    }

    //-----------------------------------
    // creating new entries
    //-----------------------------------
    public void insertUser(User u) {
        user_collection().save(u);
    }

    public void insertPlayer(Player p) {
        player_collection().save(p);
    }

    public void insertMatch(Match m) {
        WriteResult w = match_collection().save(m);
    }

    //------------------------------------
    // finding entries by their ID
    //------------------------------------
    public User findUser(String firebaseID) {
        return user_collection().findOne("{firebaseID: #}", firebaseID).as(User.class);
    }

    public User findUserByName(String username) {
        return user_collection().findOne("{username: #}", username).as(User.class);
    }

    public Player findPlayer(String firebaseID) {
        return player_collection().findOne("{firebaseID: #}", firebaseID).as(Player.class);
    }

    public Match findMatch(String hostID) {
        return match_collection().findOne("{hostID: #}", hostID).as(Match.class);
    }

    //------------------------------------
    // deleting entries by their ID
    //------------------------------------
    public void removeUser(String firebaseID) {
        user_collection().remove("{firebaseID: #}", firebaseID);
    }

    public void removePlayer(String firebaseID) {
        player_collection().remove("{firebaseID: #}", firebaseID);
    }

    public void removeMatch(String hostID) {
        //removing all Objects from the player collection associated with this specific match
        player_collection().remove("{hostID: #}", hostID);

        //removing the match itself from the match collection
        match_collection().remove("{hostID: #}", hostID);
    }

    //------------------------------------
    // Updating User-data
    //------------------------------------
    public void updateUser_changeUsername(String firebaseID, String username) {
        user_collection().update("{firebaseID: #}", firebaseID).with("{$set: {username: #}}", username);
    }

    public void updateUser_incMatches(String firebaseID) {
        user_collection().update("{firebaseID: #}", firebaseID).with("{$inc: {matches: 1}}");
    }

    public void updateUser_incWins(String firebaseID) {
        user_collection().update("{firebaseID: #}", firebaseID).with("{$inc: {wins: 1}}");
    }

    public void updateUser_addFriend(String firebaseID_user, String firebaseID_friend) {
        user_collection().update("{firebaseID: #}", firebaseID_user).with("{$addToSet: {friends: #}}", firebaseID_friend);
    }

    public void updateUser_removeFriend(String firebaseID_user, String firebaseID_friend) {
        user_collection().update("{firebaseID: #}", firebaseID_user).with("{$pull: {friends: #}}", firebaseID_friend);
    }

    public void updateUser_addRival(String firebaseID_user, String firebaseID_rival) {
        user_collection().update("{firebaseID: #}", firebaseID_user).with("{$addToSet: {rivals: #}}", firebaseID_rival);
    }

    public void updateUser_removeRival(String firebaseID_user, String firebaseID_rival) {
        user_collection().update("{firebaseID: #}", firebaseID_user).with("{$pull: {rivals: #}}", firebaseID_rival);
    }

    //------------------------------------
    // Updating Match-data
    //------------------------------------

    public void updateMatch_changeStatus(String hostID) {
        Match match = findMatch(hostID);
        match_collection().update("{hostID: #}", hostID).with("{$inc: {status: 1}}");
    }

    public void updateMatch_setWinners(String hostID, String firebaseID_winner1, String firebaseID_winner2) {
        match_collection().update("{hostID: #}", hostID).with("{$addToSet: {winners: #}}", firebaseID_winner1);
        match_collection().update("{hostID: #}", hostID).with("{$addToSet: {winners: #}}", firebaseID_winner2);
    }

    //------------------------------------
    // Updating Player-data
    //------------------------------------
    public void updatePlayer_changeLocationIndex(String firebaseID, int location_index) {
        player_collection().update("{firebaseID: #}", firebaseID).with("{$set: {current_loc_index: #}}", location_index);
    }

    public void updatePlayer_incTurnNumber(String firebaseID) {
        player_collection().update("{firebaseID: #}", firebaseID).with("{$inc: {turn_number: 1}}");
    }

    public void updatePlayer_setReadyTrue(String firebaseID) {
        player_collection().update("{firebaseID: #}", firebaseID).with("{$set: {ready: true}}");
    }

    public void updatePlayer_setReadyFalse(String firebaseID) {
        player_collection().update("{firebaseID: #}", firebaseID).with("{$set: {ready: false}}");
    }

    public void updatePlayer_setFinishedTrue(String firebaseID) {
        player_collection().update("{firebaseID: #}", firebaseID).with("{$set: {finished: true}}");
    }

    public void updateBothUsers_increasePlayedWith(String firebaseID, String firebaseID2, int i) {
        increasePlayedWith(firebaseID, firebaseID2, i);
        increasePlayedWith(firebaseID2, firebaseID, i);
    }

    public int increasePlayedWith(String firebaseID, String firebaseID2, int i) {
        Map<String, Integer> gamesPlayedWith = findUser(firebaseID).getGamesPlayedWith();
        if(gamesPlayedWith == null) {
            gamesPlayedWith = new HashMap<String, Integer>();
        }
        Integer g = gamesPlayedWith.get(firebaseID2);
        int gamesPlayed = g == null ? 0 : g.intValue();
        gamesPlayedWith.put(firebaseID2, gamesPlayed + i);
        user_collection().update("{firebaseID: #}", firebaseID).with("{$set: {gamesPlayedWith: #}}", gamesPlayedWith);
        return gamesPlayed +i;
    }

    public void updateBothUsers_increaseJokersWith(String firebaseID, String firebaseID2, int i) {
        increaseJokersWith(firebaseID, firebaseID2, i);
        increaseJokersWith(firebaseID2, firebaseID, i);
    }

    public void increaseJokersWith(String firebaseID, String firebaseID2, int i) {
        Map<String, Integer> jokersWith = findUser(firebaseID).getJokersWith();
        if(jokersWith == null) {
            jokersWith = new HashMap<String, Integer>();
        }
        Integer g = jokersWith.get(firebaseID2);
        int gamesPlayed = g == null ? 0 : g.intValue();
        jokersWith.put(firebaseID2, gamesPlayed + i);
        user_collection().update("{firebaseID: #}", firebaseID).with("{$set: {jokersWith: #}}", jokersWith);
    }
}

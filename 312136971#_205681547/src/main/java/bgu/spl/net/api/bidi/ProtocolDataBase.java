package bgu.spl.net.api.bidi;

import bgu.spl.net.api.Messages.BGSMessage;
import bgu.spl.net.api.Messages.NotificationMessage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProtocolDataBase {

    private HashMap<String, String> accounts;    // key = userName, value = password
    private HashMap<String, Boolean> whoLoggedIn;// key = userName, value = is logged in
    private HashMap<String, Integer> userHandler;// key = connectionId, value = userName
    private HashMap<String, LinkedList<String>> followers;  // key = userName, value = list of users following userName
    private HashMap<String, LinkedList<String>> followees;  // key = userName, value = list of users userName is following
    private HashMap<String, LinkedList<String>> usersPosts; // key = userName, value = list of posts
    private HashMap<String, LinkedList<String>> usersPMs;   // key = userName, value = list of private messages the user sent
    private HashMap<String, ConcurrentLinkedQueue<NotificationMessage>> unsentNotifications;// key = userName, value = queue of notifications received while offline

    public ProtocolDataBase(){
        accounts = new HashMap<>();
        whoLoggedIn = new HashMap<>();
        userHandler = new HashMap<>();
        followers = new HashMap<>();
        followees = new HashMap<>();
        usersPosts = new HashMap<>();
        usersPMs = new HashMap<>();
        unsentNotifications = new HashMap<>();
    }

    // returns true if there is already a user with that userName
    public boolean isRegistered(String userName){
        return accounts.containsKey(userName);
    }
    // adds a new user to the dataBase
    public void addAccount(String username, String password){
        accounts.put(username,password);
        whoLoggedIn.put(username, false);
        followers.put(username, new LinkedList<>());
        followees.put(username, new LinkedList<>());
        usersPosts.put(username, new LinkedList<>());
        usersPMs.put(username, new LinkedList<>());
        unsentNotifications.put(username, new ConcurrentLinkedQueue<>());
    }
    // returns true if the userName matches the password
    public boolean isMatching(String userName, String password){
        return accounts.get(userName).equals(password);
    }
    // returns true if the user is logged in
    public boolean isLoggedIn(String userName) { return whoLoggedIn.get(userName); }
    // connects between a userName and a connectionHandler
    public void connect(Integer connectionId, String userName){
        userHandler.put(userName, connectionId);
    }
    // signals that a user logged in
    public void logIn(String userName){
        whoLoggedIn.compute(userName, (user, bool) -> bool = true );
    }
    // signals that a user logged out
    public void logOut(String userName){
        whoLoggedIn.compute(userName, (user, bool) -> bool = false );
        userHandler.remove(userName);
    }
    // tries to make follower follow followee and returns true if succeeds
    public boolean addFollower(String follower, String followee){
        LinkedList<String> thisFollowers = followers.get(followee);
        if(thisFollowers.contains(follower))
            return false;
        thisFollowers.add(follower);
        followees.get(follower).add(followee);
        return true;
    }
    // tries to remove follower from followee follow list
    public boolean removeFollower(String follower, String followee) {
        followees.get(follower).remove(followee);
        return followers.get(followee).remove(follower);
    }
    // returns a list of users who follow userName
    public LinkedList<String> getFollowers(String userName) {
        return followers.get(userName);
    }
    // returns the connection Id of user
    public int getId(String user) { return userHandler.get(user); }
    // saves the user's post in the dataBase
    public void savePost(String user, String content){ usersPosts.get(user).add(content); }
    // saves the user's PM in the dataBase
    public void saveMessage(String user, String content) { usersPMs.get(user).add(content); }
    // returns a list of all userNames
    public LinkedList<String> getUsers(){ return new LinkedList<>(accounts.keySet()); }
    // returns all data required for the Stat command
    public String getStatData(String userName) {
        String str ="";
        str+= (usersPosts.get(userName).size()) + " ";
        str+= (followers.get(userName).size()) + " ";
        str+= (followees.get(userName).size()) + " ";
        return str;
    }
    public void addUnsentNotification(String user, NotificationMessage noti) {
        unsentNotifications.get(user).add(noti);
    }

    public ConcurrentLinkedQueue<NotificationMessage> getUnsentMessages(String user) {
        return unsentNotifications.get(user);
    }
}

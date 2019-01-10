package bgu.spl.net.api.bidi;

import bgu.spl.net.api.Messages.NotificationMessage;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProtocolDataBase {

    private ConcurrentHashMap<String, String> accounts;    // key = userName, value = password
    private ConcurrentHashMap<String, Boolean> whoLoggedIn;// key = userName, value = is logged in
    private ConcurrentHashMap<String, Integer> userHandler;// key = connectionId, value = userName
    private ConcurrentHashMap<String, LinkedList<String>> followers;  // key = userName, value = list of users following userName
    private ConcurrentHashMap<String, LinkedList<String>> followees;  // key = userName, value = list of users userName is following
    private ConcurrentHashMap<String, LinkedList<String>> usersPosts; // key = userName, value = list of posts
    private ConcurrentHashMap<String, LinkedList<String>> usersPMs;   // key = userName, value = list of private messages the user sent
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<NotificationMessage>> unsentNotifications;// key = userName, value = queue of notifications received while offline
    private Object registerLock;
    private Object multiLock;

    public ProtocolDataBase(){
        accounts = new ConcurrentHashMap<>();
        whoLoggedIn = new ConcurrentHashMap<>();
        userHandler = new ConcurrentHashMap<>();
        followers = new ConcurrentHashMap<>();
        followees = new ConcurrentHashMap<>();
        usersPosts = new ConcurrentHashMap<>();
        usersPMs = new ConcurrentHashMap<>();
        unsentNotifications = new ConcurrentHashMap<>();
        registerLock = new Object();
        multiLock = new Object();
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
    public boolean isLoggedIn(String userName) {
        if(!isRegistered(userName))
            return false;
        return whoLoggedIn.get(userName); }
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
        if(!accounts.containsKey(followee))
            return false;
        LinkedList<String> thisFollowers = followers.get(followee);
        if(thisFollowers.contains(follower))
            return false;
        thisFollowers.add(follower);
        followees.get(follower).add(followee);
        return true;
    }
    // tries to remove follower from followee follow list
    public boolean removeFollower(String follower, String followee) {
        if(!accounts.containsKey(followee))
            return false;
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
        str+= (followees.get(userName).size());
        return str;
    }
    public void addUnsentNotification(String user, NotificationMessage noti) {
        unsentNotifications.get(user).add(noti);
    }

    public ConcurrentLinkedQueue<NotificationMessage> getUnsentMessages(String user) {
        return unsentNotifications.get(user);
    }

    public Object getRegisterLock() {
        return registerLock;
    }

    public Object getMultiLock() {
        return multiLock;
    }
}

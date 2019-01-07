package bgu.spl.net.api.bidi;

import bgu.spl.net.api.Messages.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BidiMessageProtocolBGS implements BidiMessagingProtocol<BGSMessage> {
    // ---------------  fields  -----------
    private boolean shouldTerminate = false;
    private int connectionId;
    private boolean isLoggedIn = false;
    private Connections<BGSMessage> connections;
    private ProtocolDataBase dataBase;
    private String currentUser;
    //  ---------------  methods  -----------------
    public BidiMessageProtocolBGS(ProtocolDataBase dataBase){
        this.dataBase = dataBase;
    }
    public boolean shouldTerminate() { return shouldTerminate; }

    public void start(int connectionId, Connections<BGSMessage> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    //      Processing messages
    public void process(BGSMessage message) {
        switch (message.getOpCode()) {
            case 1: processRegister(message);
            case 2: processLogin(message);
            case 3: processLogout(message);
            case 4: processFollow(message);
            case 5: processPost(message);
            case 6: processPM(message);
            case 7: processUserList(message);
            case 8: processStat(message);
        }
    }
    //      Register
    private void processRegister(BGSMessage message) {
        String userName = ((RegisterMessage)message).getUserName();
        String password = ((RegisterMessage)message).getPassword();
        if(dataBase.isRegistered(userName)) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        dataBase.addAccount(userName, password);
        connections.send(connectionId, new ACKMessage(message.getOpCode()));
    }
    //      Login
    private void processLogin(BGSMessage message) {
        String userName = ((LoginMessage)message).getUserName();
        String password = ((LoginMessage)message).getPassword();

        if(!dataBase.isRegistered(userName) ||
        !dataBase.isMatching(userName,password) ||
         dataBase.isLoggedIn(userName) || isLoggedIn)
        {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        isLoggedIn = true;
        dataBase.connect(connectionId, userName);
        dataBase.logIn(userName);
        currentUser = userName;
        //  getting all notifications that happened when the user was logged out
        ConcurrentLinkedQueue<NotificationMessage> notifications = dataBase.getUnsentMessages(currentUser);
        for(NotificationMessage noti: notifications)
            connections.send(connectionId, noti);
    }
    //      Logout
    private void processLogout(BGSMessage message) {
        if(!isLoggedIn) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        isLoggedIn = false;
        dataBase.logOut(currentUser);
        connections.send(connectionId, new ACKMessage(message.getOpCode()));
    }
    //      Follow
    private void processFollow(BGSMessage message) {
        if(!isLoggedIn) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }

        int numOfFollows = ((FollowMessage)message).getNumOfUsers();
        LinkedList<String> toFollow = ((FollowMessage)message).getUsers();
        int success = 0;

        StringBuilder userList = new StringBuilder(numOfFollows+" ");
        if(((FollowMessage)message).isFollow())
            success = addFollows(numOfFollows, toFollow, success, userList);
        else
            success = removeFollows(numOfFollows, toFollow, success, userList);
        userList.deleteCharAt(userList.length()-1); // deleting last space
        if(success == 0) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        ACKMessage ack = new ACKMessage(message.getOpCode());
        ack.setOptionalData(userList.toString());
        connections.send(connectionId, ack);
    }
    //      Post
    private void processPost(BGSMessage message) {
        if(!isLoggedIn) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        LinkedList<String> taggedUsers = new LinkedList<>();
        String post = ((PostMessage)message).getPost();
        findTags(taggedUsers, post);
        // creating a set of users to send the post to
        Set<String> whoToSendTo = new HashSet<>();
        whoToSendTo.addAll(dataBase.getFollowers(currentUser));
        whoToSendTo.addAll(taggedUsers);
        // sending the post to all users following/tagged in the post
        for(String user: whoToSendTo)
        {
            NotificationMessage noti = new NotificationMessage(currentUser, post, '1');
            if(dataBase.isLoggedIn(user))
                connections.send(dataBase.getId(user), noti);
            else
                dataBase.addUnsentNotification(user, noti);
        }
        dataBase.savePost(currentUser, post);
    }

    //      PM
    private void processPM(BGSMessage message) {
        if(!isLoggedIn) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        String pMessage = ((PMMessage)message).getContent();
        String receiver = ((PMMessage)message).getUserName();
        NotificationMessage noti = new NotificationMessage(currentUser, pMessage, '0');
        if(dataBase.isLoggedIn(receiver))
            connections.send(dataBase.getId(receiver), noti);
        else
            dataBase.addUnsentNotification(receiver, noti);
        dataBase.saveMessage(currentUser, pMessage);
    }
    //      UserList
    private void processUserList(BGSMessage message){
        if(!isLoggedIn) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        LinkedList<String> userList = dataBase.getUsers();
        ACKMessage ack = new ACKMessage(message.getOpCode());

        StringBuilder users = new StringBuilder();
        short listSize=(short)userList.size();
        users.append(listSize).append(" ");
        for (String user: userList)
            users.append(user).append(" ");
        users.deleteCharAt(users.length()-1);   // deleting last space

        ack.setOptionalData(users.toString());
        connections.send(connectionId, ack);
    }
    //      Stats
    private void processStat(BGSMessage message) {
        String userName = ((StatMessage)message).getUser();
        if(!isLoggedIn || !dataBase.isRegistered(userName)) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        ACKMessage ack = new ACKMessage(message.getOpCode());
        ack.setOptionalData(dataBase.getStatData(userName));
        connections.send(connectionId, ack);
    }



    //          Private Methods
    // finding all userNames tagged in a post
    private void findTags(LinkedList<String> taggedUsers, String post) {
        int lastIndex = post.indexOf('@');
        while(lastIndex!=-1)
        {
            taggedUsers.add(post.substring(lastIndex+1, post.indexOf(' ', lastIndex)));
            lastIndex = post.indexOf('@', lastIndex+1);
        }
    }
    // tries to follow all users
    private int addFollows(int numOfFollows, LinkedList<String> toFollow, int success, StringBuilder userList) {
        for (int i = 0; i < numOfFollows; i++)
            if (dataBase.addFollower(currentUser, toFollow.get(i))) {
                success++;
                userList.append(toFollow.get(i)).append(" ");
            }
        return success;
    }
    // tries to unfollow all users
    private int removeFollows(int numOfFollows, LinkedList<String> toFollow, int success, StringBuilder userList) {
        for( int i=0; i < numOfFollows; i++)
            if (dataBase.removeFollower(currentUser, toFollow.get(i))) {
                success++;
                userList.append(toFollow.get(i)).append(" ");
            }
        return success;
    }
}

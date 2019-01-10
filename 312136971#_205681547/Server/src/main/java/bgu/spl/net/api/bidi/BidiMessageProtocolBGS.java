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
                break;
            case 2: processLogin(message);
                break;
            case 3: processLogout(message);
                break;
            case 4: processFollow(message);
                break;
            case 5: processPost(message);
                break;
            case 6: processPM(message);
                break;
            case 7: processUserList(message);
                break;
            case 8: processStat(message);
                break;
            case 99:                                    // disconnecting the client from the server after LOGOUT
                connections.disconnect(connectionId);
                break;
        }
    }
    //      Register
    private void processRegister(BGSMessage message) {
        if(isLoggedIn) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        String userName = ((RegisterMessage)message).getUserName();
        String password = ((RegisterMessage)message).getPassword();
        synchronized (dataBase.getRegisterLock()) {
            if (dataBase.isRegistered(userName)) {
                connections.send(connectionId, new ErrorMessage(message.getOpCode()));
                return;
            }
            dataBase.addAccount(userName, password);
        }
        connections.send(connectionId, new ACKMessage(message.getOpCode()));
    }
    //      Login
    private void processLogin(BGSMessage message) {
        String userName = ((LoginMessage)message).getUserName();
        String password = ((LoginMessage)message).getPassword();

        synchronized (dataBase.getMultiLock()) {
            if(!dataBase.isRegistered(userName) ||
            !dataBase.isMatching(userName,password) ||
             dataBase.isLoggedIn(userName) || isLoggedIn)
            {
                connections.send(connectionId, new ErrorMessage(message.getOpCode()));
                return;
            }
            dataBase.logIn(userName);
        }

        isLoggedIn = true;
        dataBase.connect(connectionId, userName);
        currentUser = userName;
        connections.send(connectionId, new ACKMessage(message.getOpCode()));
        //  getting all notifications that happened when the user was logged out
        ConcurrentLinkedQueue<NotificationMessage> notifications = dataBase.getUnsentMessages(currentUser);
        for(NotificationMessage noti: notifications)
            connections.send(connectionId, noti);
    }
    //      Logout
    private void processLogout(BGSMessage message) {
        synchronized (dataBase.getMultiLock()) {
            if(!isLoggedIn) {
                connections.send(connectionId, new ErrorMessage(message.getOpCode()));
                return;
            }
            isLoggedIn = false;
            dataBase.logOut(currentUser);
            connections.send(connectionId, new ACKMessage(message.getOpCode()));
        }
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

        StringBuilder userList = new StringBuilder();
        if(((FollowMessage)message).isFollow())
            success = addFollows(numOfFollows, toFollow, success, userList);
        else
            success = removeFollows(numOfFollows, toFollow, success, userList);

        if(success == 0) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        userList.insert(0, success+" ");
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

        connections.send(connectionId, new ACKMessage(message.getOpCode()));

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
            synchronized (dataBase.getMultiLock()) {
                if(dataBase.isLoggedIn(user))
                    connections.send(dataBase.getId(user), noti);
                else
                    dataBase.addUnsentNotification(user, noti);
            }
        }
        dataBase.savePost(currentUser, post);
    }

    //      PM
    private void processPM(BGSMessage message) {
        if(!isLoggedIn) {
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        String receiver = ((PMMessage)message).getUserName();
        if(!dataBase.isRegistered(receiver)) {               // sending error in case there is no such user
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        connections.send(connectionId, new ACKMessage(message.getOpCode()));
        // making sure no illegal tags happened inside the PM
        String pMessage = ((PMMessage)message).getContent();
        if(findIllegalTags(pMessage)){
            connections.send(connectionId, new ErrorMessage(message.getOpCode()));
            return;
        }
        NotificationMessage noti = new NotificationMessage(currentUser, pMessage, '0');
        synchronized (dataBase.getMultiLock()) {
            if(dataBase.isLoggedIn(receiver))                // sending the PM if the user is logged in
                connections.send(dataBase.getId(receiver), noti);
            else                                             // saving the PM if the user is not logged in, so he can receive it when he does log in
                dataBase.addUnsentNotification(receiver, noti);
        }
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
        // adding num of users
        short listSize=(short)userList.size();
        users.append(listSize).append(" ");
        // adding names of users
        for (String user: userList)
            users.append(user).append(" ");
        users.deleteCharAt(users.length()-1);       // deleting last space after the names list
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
    // finding illegal tags in a PM
    private boolean findIllegalTags(String pMessage) {
        int atIndex = pMessage.indexOf('@');
        while(atIndex != -1) {
            String tag = pMessage.substring(atIndex+1, pMessage.indexOf(' ',atIndex));
            if(dataBase.isRegistered(tag))
                return true;
            atIndex = pMessage.indexOf('@', atIndex+1);
        }
        return false;
    }

    // finding all userNames tagged in a post
    private void findTags(LinkedList<String> taggedUsers, String post) {
        int lastIndex = post.indexOf('@');
        while(lastIndex!=-1)
        {
            if(post.indexOf((' '), lastIndex) == -1) {
                taggedUsers.add(post.substring(lastIndex + 1));       // in case the post contained no content and only tags
                return;
            }
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
        if(success > 0)
            userList.deleteCharAt(userList.length()-1); // deleting last space
        return success;
    }
    // tries to unfollow all users
    private int removeFollows(int numOfFollows, LinkedList<String> toFollow, int success, StringBuilder userList) {
        for( int i=0; i < numOfFollows; i++)
            if (dataBase.removeFollower(currentUser, toFollow.get(i))) {
                success++;
                userList.append(toFollow.get(i)).append(" ");
            }
        if(success > 0)
            userList.deleteCharAt(userList.length()-1); // deleting last space
        return success;
    }
}

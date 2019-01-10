package bgu.spl.net.api.Messages;

import java.util.LinkedList;

public class FollowMessage implements BGSMessage {

    private final short OPCODE = 4;

    private LinkedList<String> users;
    private boolean follow;
    private int numOfUsers;
    private int numOfZeros;
    private int lastZeroIndex;

    public FollowMessage() {
        numOfZeros=0;
        numOfUsers = 0;
        users=new LinkedList<>();
        follow=false;
        lastZeroIndex = 4;
    }

    public short getOpCode() { return OPCODE; }

    public LinkedList<String> getUsers(){ return users; }
    public void addUser(String user){ users.add(user); }

    public void setFollow(boolean follow){ this.follow=follow; }
    public boolean isFollow() { return follow; }

    public int getNumOfZeros() { return numOfZeros; }
    public void increaseNumOfZeros(){ numOfZeros++; }

    public int getNumOfUsers() { return numOfUsers; }
    public void setNumOfUsers(int numOfUsers) { this.numOfUsers = numOfUsers; }

    public int getLastZeroIndex() { return lastZeroIndex; }
    public void setLastZeroIndex(int index) { lastZeroIndex = index; }
}